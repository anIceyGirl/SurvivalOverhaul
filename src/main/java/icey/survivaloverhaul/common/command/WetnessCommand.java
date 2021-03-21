package icey.survivaloverhaul.common.command;

import java.util.UUID;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import icey.survivaloverhaul.common.capability.wetness.WetnessCapability;
import icey.survivaloverhaul.util.CapabilityUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class WetnessCommand extends CommandBase
{

	public WetnessCommand() 
	{
		super(Commands.literal("wetness").requires((p_198521_0_) -> {
			return p_198521_0_.hasPermission(2);
		})
				.then(Commands.literal("set").then(Commands.argument("wetness", IntegerArgumentType.integer(0, WetnessCapability.WETNESS_LIMIT)).executes(src -> new WetnessCommand().set(src.getSource(), IntegerArgumentType.getInteger(src, "wetness")))))
				.then(Commands.literal("get").executes(src -> new WetnessCommand().get(src.getSource())))
		);
	}
	
	private int set(CommandSource src, int wetness) throws CommandSyntaxException 
	{
		CapabilityUtil.getWetnessCapability(src.getPlayerOrException()).setWetness(wetness);
		return 1;
	}
	
	private int get(CommandSource src) throws CommandSyntaxException 
	{
		String msg = "Wetness: " + CapabilityUtil.getWetnessCapability(src.getPlayerOrException()).getWetness();
		
		src.getPlayerOrException().sendMessage(new StringTextComponent((msg)), UUID.randomUUID());
		return 1;
	}
}
