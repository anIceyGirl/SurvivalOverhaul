package icey.survivaloverhaul.registry;

import java.lang.reflect.Field;

import icey.survivaloverhaul.Main;
import icey.survivaloverhaul.api.temperature.ModifierBase;
import icey.survivaloverhaul.common.temperature.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TemperatureModifierRegistry
{
	public static final ModifierBase DEFAULT = new DefaultModifier();
	public static final ModifierBase BIOME = new BiomeModifier();
	public static final ModifierBase TIME = new TimeModifier();
	public static final ModifierBase ALTITUDE = new AltitudeModifier();
	public static final ModifierBase TEMPORARY = new PlayerTemporaryModifier();
	public static final ModifierBase SPRINT = new SprintModifier();
	public static final ModifierBase BLOCKS = new BlockModifier();
	public static final ModifierBase ARMOR = new ArmorModifier();
	public static final ModifierBase ON_FIRE = new OnFireModifier();
	public static final ModifierBase WEATHER = new WeatherModifier();
	
	@SubscribeEvent
	public static void registerModifiers(RegistryEvent.Register<ModifierBase> event)
	{
		try
		{
			for (Field f : TemperatureModifierRegistry.class.getDeclaredFields()) 
			{
				Object obj = f.get(null);
				if (obj instanceof ModifierBase) 
				{
					event.getRegistry().register((ModifierBase) obj);
				} 
				else if (obj instanceof ModifierBase[]) 
				{
					for (ModifierBase modifier : (ModifierBase[]) obj) 
					{
						event.getRegistry().register(modifier);
					}
				}
			}
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
