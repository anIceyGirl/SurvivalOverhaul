package icey.survivaloverhaul.common.temperature;

import java.util.List;
import java.util.Map;

import icey.survivaloverhaul.api.config.json.temperature.JsonPropertyTemperature;
import icey.survivaloverhaul.api.config.json.temperature.JsonTemperature;
import icey.survivaloverhaul.api.temperature.ITemperatureTileEntity;
import icey.survivaloverhaul.api.temperature.ModifierBase;
import icey.survivaloverhaul.config.Config;
import icey.survivaloverhaul.config.json.JsonConfig;
import icey.survivaloverhaul.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BlockModifier extends ModifierBase
{
	private float coldestValue = 0.0f;
	private float hottestValue = 0.0f;
	private float hotTotal = 0.0f;
	private float coldTotal = 0.0f;

	public BlockModifier()
	{
		super();
	}
	
	@Override
	public float getWorldInfluence(World world, BlockPos pos)
	{
		coldestValue = 0.0f;
		hottestValue = 0.0f;
		
		hotTotal = 0.0f;
		coldTotal = 0.0f;
		
		doBlocksRoutine(world, pos);
		doFluidRoutine(world, pos);
		doTileEntitiesRoutine(world, pos);
		
		hotTotal -= hottestValue;
		coldTotal -= coldestValue;
		
		float hotLogValue = hottestValue * (float)Math.sqrt(easyLog(hotTotal));
		float coldLogValue = coldestValue * (float)Math.sqrt(easyLog(coldTotal));
		
		float result = hotLogValue + coldLogValue;

		if(result > hottestValue)
		{
			//Hotter than hottestValue, clamp
			return Math.min(hottestValue + 2.5f, result);
		}
		else if(result < coldestValue)
		{
			//Colder than coldestValue, clamp
			return Math.max(coldestValue - 2.5f, result);
		}
		else
		{
			//Within bounds, no need to clamp
			return result;
		}
	}
	
	private void doBlocksRoutine(World world, BlockPos pos)
	{
		// We don't use a MutableBoundingBox since it's easier to conceptualize
		
		int horizontalDist = Config.Baked.tempInfluenceHorizontalDist;
		int verticalDist = Config.Baked.tempInfluenceVerticalDist;
		
		for (int x = -horizontalDist; x <= horizontalDist; x++)
		{
			for (int y = -verticalDist; y <= verticalDist; y++)
			{
				for (int z = -horizontalDist; z <= horizontalDist; z++)
				{
					final BlockPos blockPos = pos.offset(x, y, z);
					final BlockState blockState = world.getBlockState(blockPos);
					final Block block = blockState.getBlock();
					
					List<JsonPropertyTemperature> tempInfoList = JsonConfig.blockTemperatures.get(block.getRegistryName().toString());
					
					// jsons seem to work fine without getActualState
					// i guess matchesState gets the actual state now. 
					
					if (tempInfoList != null)
					{
						for (JsonPropertyTemperature tempInfo : tempInfoList)
						{
							if (tempInfo == null)
									continue;
							
							float blockTemp = tempInfo.temperature;
							
							if (blockTemp == 0.0f)
									continue;
							
							if (tempInfo.matchesState(blockState))
							{
								processTemp(blockTemp);
								continue;
							}
						}
					}
				}
			}
		}
	}
	
	private void doFluidRoutine(World world, BlockPos pos)
	{
		int horizontalDist = Config.Baked.tempInfluenceHorizontalDist;
		int verticalDist = Config.Baked.tempInfluenceVerticalDist;

		for (int x = -horizontalDist; x <= horizontalDist; x++)
		{
			for (int y = -verticalDist; y <= verticalDist; y++)
			{
				for (int z = -horizontalDist; z <= horizontalDist; z++)
				{
					final BlockPos blockPos = pos.offset(x, y, z);
					final FluidState fluidState = world.getFluidState(blockPos);
					final Fluid fluid = fluidState.getType();
					
					for (Map.Entry<String, JsonTemperature> entry : JsonConfig.fluidTemperatures.entrySet())
					{
						if (entry.getValue() == null)
								continue;
						
						if (entry.getKey().contentEquals(fluid.getRegistryName().toString()))
						{
							processTemp(entry.getValue().temperature);
						}
					}
				}
			}
		}
	}
	
	private void doTileEntitiesRoutine(World world, BlockPos pos)
	{
		for (int x = -3; x <= 3; x++)
		{
			for (int z = -3; z <= 3; z++)
			{
				checkChunkAndProcess(world, pos.offset(x * 16, 0, z * 16), pos);
			}
		}
	}
	
	private void processTemp(float temp)
	{
		if (temp == 0.0f)
				return;
		
		if (temp >= 0.0f)
		{
			hotTotal += temp;
			if (temp > hottestValue)
			{
				hottestValue = temp;
			}
		}
		else
		{
			coldTotal += temp;
			if (temp < coldestValue)
			{
				coldestValue = temp;
			}
		}
	}
	
	private void checkChunkAndProcess(World world, BlockPos pos, BlockPos selfPos)
	{
		try
		{
			if (WorldUtil.isChunkLoaded(world, pos))
			{
				Chunk chunk = world.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, false);
				
				for (Map.Entry<BlockPos, TileEntity> entry : chunk.getBlockEntities().entrySet())
				{
					processTemp(checkTileEntity(world, entry.getKey(), entry.getValue(), selfPos));
				}
			}
		}
		catch (Exception e)
		{
			
		}
	}
	
	private float checkTileEntity(World world, BlockPos pos, TileEntity tileEntity, BlockPos selfPos)
	{
		double distance = pos.distSqr(selfPos);
		if (distance < 2500.0d)
		{
			// Within 50 blocks
			
			if (tileEntity instanceof ITemperatureTileEntity)
			{
				return ((ITemperatureTileEntity) tileEntity).getInfluence(selfPos, distance);
			}
		}
		
		return 0.0f;
	}
	
	private float easyLog(float f)
	{
		if(f >= 0.0f)
		{
			return (float)Math.log10(f + 10.0f);
		}
		else
		{
			return (float)Math.log10(-1.0f * f + 10.0f);
		}
	}
}