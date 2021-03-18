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
	private int temperature;
	
	private static final VoxelShape[] SHAPES = new VoxelShape[]
			{
					Block.makeCuboidShape(0d, 0d, 0d, 16d, 8d, 16d), // DOWN
					//Block.makeCuboidShape(1,2,3,4,5,6), // UP
					//Block.makeCuboidShape(1,2,3,4,5,6), // NORTH
					//Block.makeCuboidShape(1,2,3,4,5,6), // SOUTH
					//Block.makeCuboidShape(1,2,3,4,5,6), // WEST
					//Block.makeCuboidShape(1,2,3,4,5,6), // EAST
			};

	public BlockTemperatureSensor() 
	{
		super(AbstractBlock.Properties.create(Material.WOOD).zeroHardnessAndResistance().notSolid());
		this.setDefaultState(this.stateContainer.getBaseState().with(DIRECTION, Direction.DOWN).with(INVERTED, Boolean.valueOf(false)));
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) 
	{
		try 
		{
			int templ = 0;
			//should give an option for a binary response
			if (!state.get(INVERTED)) //false
				templ = TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(worldIn, pos)) / 2;
			else
				templ = 15 / TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(worldIn, pos));
			
			if (temperature != templ) 
			{
				temperature = templ;
				Blocks.REDSTONE_WIRE.getDefaultState().neighborChanged(worldIn, pos, getSelf(), pos, false);//possibly not efficient
				//System.out.println("Change Temp");
			}
			
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 15);
		}
		catch(Exception e) 
		{
			Main.LOGGER.error(e);
		}
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) 
	{
		builder.add(DIRECTION).add(INVERTED);
	}
	
	@Override
	public boolean canProvidePower (BlockState state) 
	{    
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) 
	{
		return SHAPES[0];
	}
	
	@Override
    public int getWeakPower (BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) 
	{
        return temperature;
    }
	
	@Override
    public int getStrongPower (BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) 
	{
        return temperature;
    }
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) 
	{
		
		//server only?
		if (state.get(INVERTED))//true
			worldIn.setBlockState(pos, state.with(INVERTED, false));
		else
			worldIn.setBlockState(pos, state.with(INVERTED, true));
		//System.out.println("INVERTED");
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) 
	{	//something here is null on server when getWorldTemperature is called (same in tick). guessing pos?
		try 
		{
			temperature = TemperatureUtil.clampTemperature(TemperatureUtil.getWorldTemperature(worldIn, pos)) / 2;
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 15);
		}
		catch(Exception e) 
		{
			Main.LOGGER.error(e);
		}
	}
}