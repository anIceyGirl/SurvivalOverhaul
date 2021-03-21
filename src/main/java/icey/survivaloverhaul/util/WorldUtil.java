package icey.survivaloverhaul.util;

import icey.survivaloverhaul.api.temperature.TemperatureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public final class WorldUtil
{
	private WorldUtil() {}
	
	public static BlockPos getSidedBlockPos(World world, Entity entity)
	{
		if(!world.isClientSide)
		{
			return entity.blockPosition();
		}
		
		if(entity instanceof PlayerEntity)
		{
			return new BlockPos(entity.position().add(0, 0.5d, 0));
		}
		else if(entity instanceof ItemFrameEntity)
		{
			return new BlockPos(entity.position().add(0, -0.45d, 0));
		}
		else
		{
			return entity.blockPosition();
		}
	}
	
	public static boolean isChunkLoaded(World world, BlockPos pos)
	{
		if(world.isClientSide)
		{
			return true;
		}
		else
		{
			return ((ServerWorld) world).getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
		}
	}
	
	public static int calculateClientWorldEntityTemperature(World world, Entity entity)
	{
		return TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(world, getSidedBlockPos(world, entity)));
	}
}
