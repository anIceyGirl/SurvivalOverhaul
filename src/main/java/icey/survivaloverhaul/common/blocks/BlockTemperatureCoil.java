package icey.survivaloverhaul.common.blocks;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

public class BlockTemperatureCoil extends Block implements IWaterLoggable
{
	public static final DirectionProperty DIRECTION = BlockStateProperties.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	
	private static final VoxelShape[] SHAPES = new VoxelShape[]
			{
					Block.box(4.25d, 0.0d, 4.25d, 11.75d, 16.0d, 11.75d), // DOWN
					Block.box(4.25d, 0.0d, 4.25d, 11.75d, 16.0d, 11.75d), // UP
					Block.box(4.25d, 4.25d, 0.0d, 11.75d, 11.75d, 16.0d), // NORTH
					Block.box(4.25d, 4.25d, 0.0d, 11.75d, 11.75d, 16.0d), // SOUTH
					Block.box(0.0d, 4.25d, 4.25d, 16.0d,  11.75d, 11.75d), // WEST
					Block.box(0.0d, 4.25d, 4.25d, 16.00d, 11.75d, 11.75d), // EAST
			};
	
	public final CoilType coilType;
	
	public BlockTemperatureCoil(CoilType coilType)
	{
		super(AbstractBlock.Properties
				.of(Material.METAL)
				.strength(4.0f, 10.0f)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(1)
				.noOcclusion());
		this.coilType = coilType;
		
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(DIRECTION, Direction.DOWN)
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false)));
	}
	
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		if (!worldIn.isClientSide)
		{
			boolean powered = worldIn.hasNeighborSignal(pos);
			boolean enabled = state.getValue(POWERED);
			
			if (enabled && !powered)
			{
				worldIn.getBlockTicks().scheduleTick(pos, this, 4);
			}
			else if (!enabled && powered)
			{
				turnOn(worldIn, pos, state);
			}
		}
	}
	
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand)
	{
		if (state.getValue(POWERED) && !worldIn.hasNeighborSignal(pos))
		{
			turnOff(worldIn, pos, state);
		}
	}
	
	private void turnOff(final World world, final BlockPos pos, final BlockState state)
	{
		world.setBlockAndUpdate(pos, state.setValue(POWERED, false));
	}
	
	private void turnOn(final World world, final BlockPos pos, final BlockState state)
	{
		world.setBlockAndUpdate(pos, state.setValue(POWERED, true));
	}
	
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.defaultBlockState().setValue(DIRECTION, context.getClickedFace().getOpposite()).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}
	
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(DIRECTION, POWERED, WATERLOGGED);
	}
	
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPES[state.getValue(DIRECTION).get3DDataValue()];
	}
	
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state)
	{
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
	
	public enum CoilType
	{
		COOLING("cooling", -1.0f),
		HEATING("heating", 1.0f),
		BROKEN("broken", 0f);
		
		private String name;
		private float temperature;
		
		private CoilType(String name, float temperature)
		{
			this.name = name;
			this.temperature = temperature;
		}
		
		public String getName()
		{
			return name;
		}
		
		public float getTemperatureMultiplier()
		{
			return temperature;
		}
	}

	/*
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return new CoilTileEntity();
	}
	*/
}
