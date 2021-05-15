package dev.vriska.quiltlangjs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class JSBlock extends Block {
    @FunctionalInterface
    public interface OnUseImpl {
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit);
    }

    public OnUseImpl onUseImpl = null;

    public JSBlock(Block.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (onUseImpl != null) {
            return onUseImpl.onUse(state, world, pos, player, hand, hit);
        } else {
            return super.onUse(state, world, pos, player, hand, hit);
        }
    }
}
