package icey.survivaloverhaul.registry;

import com.mojang.brigadier.CommandDispatcher;

import icey.survivaloverhaul.Main;
import icey.survivaloverhaul.common.command.CommandBase;
import icey.survivaloverhaul.common.command.SOCommand;
import icey.survivaloverhaul.common.command.TemperatureCommand;
import icey.survivaloverhaul.common.command.WetnessCommand;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegister 
{
	public static final class ModCommands
	{
		public static final CommandBase TEMPERATURE = new TemperatureCommand();
		public static final CommandBase WETNESS = new WetnessCommand();
		public static final CommandBase GENERIC_INFO = new SOCommand();
	}
	
	@SubscribeEvent
	public static void onCommandRegister(RegisterCommandsEvent event)
	{
		CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();

		dispatcher.register(ModCommands.TEMPERATURE.getBuilder());
		dispatcher.register(ModCommands.WETNESS.getBuilder());
		dispatcher.register(ModCommands.GENERIC_INFO.getBuilder());
	}
}
