package icey.survivaloverhaul.common.command;

import java.util.UUID;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import icey.survivaloverhaul.Main;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class SOCommand extends CommandBase 
{

	public SOCommand() {
		super(Commands.literal("survivaloverhaul").executes(src -> new SOCommand().execute(src.getSource())));
	}
	
	public int execute(CommandSource src) throws CommandSyntaxException 
	{
		IModInfo info = ModList.get().getModContainerById(Main.MOD_ID).get().getModInfo();
		
		String msg = "Survival Overhual V. " + info.getVersion() + "\n" + info.getDescription();
		
		src.getPlayerOrException().sendMessage(new StringTextComponent((msg)), UUID.randomUUID());
		
		return 1;
	}

}
