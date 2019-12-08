package svenhjol.strange.spells.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class FreezeSpell extends Spell
{
    public FreezeSpell()
    {
        super("freeze");
        this.element = Element.WATER;
        this.affect = Affect.AREA;
        this.duration = 3.0F;
        this.castCost = 15;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castArea(player, new int[] { 6, 2, 6 }, blocks -> {
            World world = player.world;

            if (world.isRemote) return;
            boolean didAnyFreeze = false;

            for (BlockPos pos : blocks) {
                boolean didFreeze = false;
                BlockState state = world.getBlockState(pos);

                if (state == Blocks.LAVA.getDefaultState()) {
                    world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state.getBlock() == Blocks.LAVA) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state == Blocks.WATER.getDefaultState()) {
                    world.setBlockState(pos, Blocks.ICE.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state == Blocks.FIRE.getDefaultState()) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    didFreeze = true;
                } else if (state.getBlock() == Blocks.WATER) {
                    world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState(), 2);
                    didFreeze = true;
                }

                if (didFreeze) {
                    didAnyFreeze = true;
                    world.neighborChanged(pos, state.getBlock(), pos);
                }
            }

            if (didAnyFreeze) {
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            }
        });

        didCast.accept(true);
    }
}
