package fr.kerian_animals.thecollector.block;

import com.mojang.serialization.MapCodec;
import fr.kerian_animals.thecollector.block.entity.AlembicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class AlembicBlock extends BaseEntityBlock implements EntityBlock {
    public static final MapCodec<AlembicBlock> CODEC = simpleCodec(AlembicBlock::new);
    public static final BooleanProperty HAS_BOTTLE_0 = BlockStateProperties.HAS_BOTTLE_0;
    public static final BooleanProperty HAS_BOTTLE_1 = BlockStateProperties.HAS_BOTTLE_1;
    public static final BooleanProperty HAS_BOTTLE_2 = BlockStateProperties.HAS_BOTTLE_2;

    public AlembicBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HAS_BOTTLE_0, false)
                .setValue(HAS_BOTTLE_1, false)
                .setValue(HAS_BOTTLE_2, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        openMenu(level, state, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        openMenu(level, state, pos, player);
        return InteractionResult.SUCCESS;
    }

    private static void openMenu(Level level, BlockState state, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return;
        }
        net.minecraft.world.MenuProvider provider = state.getMenuProvider(level, pos);
        if (provider != null) {
            player.openMenu(provider, pos);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(HAS_BOTTLE_0, HAS_BOTTLE_1, HAS_BOTTLE_2);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, fr.kerian_animals.thecollector.registry.ModBlockEntities.ALEMBIC.get(), AlembicBlockEntity::tick);
    }
}
