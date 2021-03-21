package icey.survivaloverhaul;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.*;

import icey.survivaloverhaul.api.injury.PlayerCondition;
import icey.survivaloverhaul.api.temperature.DynamicModifierBase;
import icey.survivaloverhaul.api.temperature.ModifierBase;
import icey.survivaloverhaul.api.temperature.TemperatureEnum;
import icey.survivaloverhaul.api.temperature.TemperatureUtil;
import icey.survivaloverhaul.common.capability.heartmods.HeartModifierCapability;
import icey.survivaloverhaul.common.capability.heartmods.HeartModifierStorage;
import icey.survivaloverhaul.common.capability.temperature.TemperatureCapability;
import icey.survivaloverhaul.common.capability.temperature.TemperatureStorage;
import icey.survivaloverhaul.common.world.OreGeneration;
import icey.survivaloverhaul.common.capability.wetness.WetnessCapability;
import icey.survivaloverhaul.common.compat.sereneseasons.SereneSeasonsModifier;
import icey.survivaloverhaul.config.*;
import icey.survivaloverhaul.config.json.JsonConfigRegistration;
import icey.survivaloverhaul.network.NetworkHandler;
import icey.survivaloverhaul.registry.BlockRegistry;
import icey.survivaloverhaul.registry.EffectRegistry;
import icey.survivaloverhaul.registry.EnchantRegistry;
import icey.survivaloverhaul.registry.ItemRegistry;
import icey.survivaloverhaul.registry.TemperatureModifierRegistry;
import icey.survivaloverhaul.util.WorldUtil;
import icey.survivaloverhaul.util.internal.TemperatureUtilInternal;

@SuppressWarnings("unused")
@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class Main
{
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "survivaloverhaul";
	
	/** Serene Seasons and Better Weather both add their own seasons system,
	 *  so we'll probably want to integrate those with the temperature/climbing
	 *  system, i.e. making it so that winter is colder, summer is hotter,
	 *  and perhaps you're more prone to slipping while climbing in the winter.
	 */ 
	public static boolean betterWeatherLoaded = false;
	public static boolean sereneSeasonsLoaded = false;
	
	/**
	 * Since my mod and Survive both do very similar things, it might be
	 * a good idea to let the user know that should probably only choose
	 * one or the other unless they know what they're doing.
	 * 
	 * Also it should only show this type of warning once so that we don't
	 * annoy the player if they decide to go through with it.
	 */
	public static boolean surviveLoaded = false;
	
	public static boolean curiosLoaded = false;
	
	/**
	 * The original. The one and only. Hope.
	 */
	public static boolean toughAsNailsLoaded = false;
	
	public static Path configPath = FMLPaths.CONFIGDIR.get();
	public static Path modConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "survivaloverhaul");
	public static Path modConfigJsons = Paths.get(modConfigPath.toString(), "json");
	
	public static Path ssConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "sereneseasons");
	
	public static ForgeRegistry<ModifierBase> MODIFIERS;
	public static ForgeRegistry<DynamicModifierBase> DYNAMIC_MODIFIERS;
	public static ForgeRegistry<PlayerCondition> PLAYER_CONDITIONS;
	
	//@OnlyIn(Dist.CLIENT)//broke on server, no longer using const :(
	//public static final KeyBinding KEY_CLIMB = new KeyBinding("key." + MOD_ID + ".grab", GLFW.GLFW_KEY_R, "key.categories.inventory");
	
	public Main()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		modBus.addListener(this::setup);
		modBus.addListener(this::onModConfigEvent);
		modBus.addListener(this::buildRegistries);
		modBus.addListener(this::clientEvents);
		
		ItemRegistry.ITEMS.register(modBus);
		EffectRegistry.EFFECTS.register(modBus);
		EffectRegistry.POTIONS.register(modBus);
		EnchantRegistry.ENCHANTS.register(modBus);
		BlockRegistry.BLOCKS.register(modBus);
		TemperatureModifierRegistry.MODIFIERS.register(modBus);
		TemperatureModifierRegistry.DYNAMIC_MODIFIERS.register(modBus);
		
		forgeBus.addListener(this::serverStarted);
		forgeBus.addListener(this::reloadListener);
		
		MinecraftForge.EVENT_BUS.register(this);
		
		Config.register();
		
		Config.Baked.bakeClient();
		Config.Baked.bakeCommon();
		
		TemperatureUtil.internal = new TemperatureUtilInternal();
		modCompat();
	}
	
	private void modCompat()
	{
		sereneSeasonsLoaded = ModList.get().isLoaded("sereneseasons");
		curiosLoaded = ModList.get().isLoaded("curios");
		surviveLoaded = ModList.get().isLoaded("survive");
		
		if (sereneSeasonsLoaded)
			LOGGER.debug("Serene Seasons is loaded, enabling compatability");
		if (curiosLoaded)
			LOGGER.debug("Curios is loaded, enabling compatability");
		if (surviveLoaded)
			LOGGER.debug("Survive is loaded, I hope you know what you're doing");
	}
	
	@CapabilityInject(TemperatureCapability.class)
	public static final Capability<TemperatureCapability> TEMPERATURE_CAP = null;
	@CapabilityInject(HeartModifierCapability.class)
	public static final Capability<HeartModifierCapability> HEART_MOD_CAP = null;
	@CapabilityInject(WetnessCapability.class)
	public static final Capability<WetnessCapability> WETNESS_CAP = null;
	
	private void setup(final FMLCommonSetupEvent event)
	{
		CapabilityManager.INSTANCE.register(TemperatureCapability.class, new TemperatureStorage(), TemperatureCapability::new);
		CapabilityManager.INSTANCE.register(HeartModifierCapability.class, new HeartModifierStorage(), HeartModifierCapability::new);
		CapabilityManager.INSTANCE.register(WetnessCapability.class, new WetnessCapability.Storage(), WetnessCapability::new);
		
		NetworkHandler.register();
		// OreGeneration.register();
		
		event.enqueueWork(() -> 
		{
			EffectRegistry.registerPotionRecipes();
		});
	}
	
	@SuppressWarnings("unused")
	private void biomeModification(final BiomeLoadingEvent event)
	{
		// FeatureRegistry.biomeModification(event);
	}
	
	private void clientEvents(final FMLClientSetupEvent event)
	{
		RenderTypeLookup.setRenderLayer(BlockRegistry.COOLING_COIL.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockRegistry.HEATING_COIL.get(), RenderType.cutout());
		
		event.enqueueWork(() -> 
		{
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientModelSetup());
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientKeyBindsSetup());
		});
	}
	
	private void serverStarted(final FMLServerStartedEvent event)
	{
		if (sereneSeasonsLoaded)
			SereneSeasonsModifier.populateBiomeIdentities();
	}
	
	private static DistExecutor.SafeRunnable clientModelSetup()
	{
		return new DistExecutor.SafeRunnable()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void run()
			{
				ItemModelsProperties.register(ItemRegistry.THERMOMETER.get(), new ResourceLocation("temperature"), new IItemPropertyGetter()
					{
						@OnlyIn(Dist.CLIENT)
						@Override
						public float call(ItemStack stack, ClientWorld clientWorld, LivingEntity entity)
						{
							World world = clientWorld;
							Entity holder = (Entity) (entity != null ? entity : stack.getFrame());
							
							if (world == null && holder != null)
							{
								world = holder.level;
							}
							
							if (world == null)
							{
								return 0.5f;
							}
							else
							{
								try
								{
									double d;
									
									int temperature = WorldUtil.calculateClientWorldEntityTemperature(world, holder);
									d = (double)((float)temperature / (float)TemperatureEnum.HEAT_STROKE.getUpperBound());
									
									return MathHelper.positiveModulo((float)d, 1.0333333f);
								}
								catch (NullPointerException e)
								{
									return 0.5f;
								}
								
							}
						}
					}
				);
			}
		};
	}
	
	// public static KeyBinding KEY_CLIMB;
	
	private static DistExecutor.SafeRunnable clientKeyBindsSetup()
	{
		return new DistExecutor.SafeRunnable()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void run()
			{
				// ClientRegistry.registerKeyBinding(new KeyBinding("key." + MOD_ID + ".grab", GLFW.GLFW_KEY_R, "key.categories.inventory"));
				// KEY_CLIMB = new KeyBinding("key." + MOD_ID + ".grab", GLFW.GLFW_KEY_R, "key.categories.inventory");
			}
		};
	}
	
	private void reloadListener(final AddReloadListenerEvent event)
	{
		event.addListener(new ReloadListener<Void>() 
				{
            		@Nonnull
            		@ParametersAreNonnullByDefault
					@Override
					protected Void prepare(IResourceManager manager, IProfiler profiler)
					{
						JsonConfigRegistration.clearContainers();
						return null;
					}
            		
            		@ParametersAreNonnullByDefault
					@Override
					protected void apply(Void objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
					{
						Config.Baked.bakeCommon();
						JsonConfigRegistration.init(modConfigJsons.toFile());
					}
			
				}
		);
	}
	
	private void onModConfigEvent(final ModConfig.ModConfigEvent event)
	{
		final ModConfig config = event.getConfig();
		
		// Since client config is not shared, we want it to update instantly whenever it's saved
		if (config.getSpec() == Config.CLIENT_SPEC)
			Config.Baked.bakeClient();
	}
	
	// Create registries for modifiers and dynamic modifiers
	private void buildRegistries(final RegistryEvent.NewRegistry event)
	{
		RegistryBuilder<ModifierBase> modifierBuilder = new RegistryBuilder<ModifierBase>();
		modifierBuilder.setName(new ResourceLocation(Main.MOD_ID, "modifiers"));
		modifierBuilder.setType(ModifierBase.class);
		MODIFIERS = (ForgeRegistry<ModifierBase>) modifierBuilder.create();
		
		RegistryBuilder<DynamicModifierBase> dynamicModifierBuilder = new RegistryBuilder<DynamicModifierBase>();
		dynamicModifierBuilder.setName(new ResourceLocation(Main.MOD_ID, "dynamic_modifiers"));
		dynamicModifierBuilder.setType(DynamicModifierBase.class);
		DYNAMIC_MODIFIERS = (ForgeRegistry<DynamicModifierBase>) dynamicModifierBuilder.create();
		
		RegistryBuilder<PlayerCondition> playerConditionBuilder = new RegistryBuilder<PlayerCondition>();
		playerConditionBuilder.setName(new ResourceLocation(Main.MOD_ID, "player_conditions"));
		playerConditionBuilder.setType(PlayerCondition.class);
		PLAYER_CONDITIONS = (ForgeRegistry<PlayerCondition>) playerConditionBuilder.create();
	}
}
