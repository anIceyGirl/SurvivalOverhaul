package icey.survivaloverhaul.common.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import icey.survivaloverhaul.Main;
import icey.survivaloverhaul.api.temperature.TemperatureUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BlockTemperatureSensor extends Block 
{
	public static final DirectionProperty DIRECTION = BlockStateProperties.FACING;
	public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
	private int temperature = 0;

	public BlockTemperatureSensor() 
	{
		super(AbstractBlock.Properties.of(Material.WOOD).instabreak().noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION, Direction.DOWN).setValue(INVERTED, Boolean.valueOf(false)));
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) 
	{
		try 
		{
			int templ = 0;
			if (!state.getValue(INVERTED)) //false
				templ = TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(worldIn, pos)) / 2;
			else
				templ = 15 / TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(worldIn, pos));
			
			if (temperature != templ) 
			{
				temperature = templ;
				Blocks.REDSTONE_WIRE.defaultBlockState().neighborChanged(worldIn, pos, asBlock(), pos, false);//possibly not efficient
			}
			
			worldIn.getBlockTicks().scheduleTick(pos, this, 15);
		}
		catch(Exception e) 
		{
			Main.LOGGER.error(e);
		}
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) 
	{
		builder.add(DIRECTION).add(INVERTED);
	}
	
	@Override
	public boolean isSignalSource (BlockState state) 
	{    
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) 
	{
		return Block.box(0d, 0d, 0d, 16d, 8d, 16d);
	}
	
	@Override
    public int getSignal (BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) 
	{
        return temperature;
    }
	
	@Override
    public int getDirectSignal (BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) 
	{
        return temperature;
    }
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) 
	{
		
		//server only?
		if (state.getValue(INVERTED))//true
			worldIn.setBlockAndUpdate(pos, state.setValue(INVERTED, false));
		else
			worldIn.setBlockAndUpdate(pos, state.setValue(INVERTED, true));
		//System.out.println("INVERTED");
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) 
	{	
		//for some reason the biome registry name is null on dedicated server when getWorldTemperature is called.

		if (worldIn.getBiome(pos).getRegistryName() == null) 
		{
			worldIn.getBlockTicks().scheduleTick(pos, this, 15);// after initial placement it fixes itself 
			return;
		}
		temperature = TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(worldIn, pos)) / 2;
		worldIn.getBlockTicks().scheduleTick(pos, this, 15);
	}
}