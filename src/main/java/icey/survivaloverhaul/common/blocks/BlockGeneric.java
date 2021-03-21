package icey.survivaloverhaul.common.blocks;


import icey.survivaloverhaul.util.OreGenerationUtil.OreOptions;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

import net.minecraft.block.AbstractBlock;

public class BlockGeneric extends Block
{
	public OreOptions OO;
	
	public BlockGeneric(Material materialIn, String toolUsed, int toolStrength, float hardness, float resistance, SoundType sound) 
	{
		this(materialIn, toolUsed, toolStrength, resistance, resistance, sound, null);
	}
	
	public BlockGeneric(Material materialIn, String toolUsed, int toolStrength, float hardness, float resistance, SoundType sound, OreOptions OO)
	{
		super(
			AbstractBlock.Properties
				.of(materialIn)
				.sound(sound)
				.strength(hardness, resistance)
				.harvestTool(ToolType.get(toolUsed))
			);
		if (OO != null)
			this.OO = OO;
	}
	public BlockGeneric get() {return this;}
}
