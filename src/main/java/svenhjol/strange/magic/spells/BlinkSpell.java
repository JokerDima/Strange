package svenhjol.strange.magic.spells;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class BlinkSpell extends Spell
{
    public BlinkSpell()
    {
        super("blink");
        this.element = Element.AIR;
        this.affect = Affect.TARGET;
        this.applyCost = 2;
        this.duration = 1.0F;
        this.castCost = 5;
    }

    @Override
    public void cast(PlayerEntity player, ItemStack staff, Consumer<Boolean> didCast)
    {
        this.castTarget(player, (result, beam) -> {
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                World world = player.world;
                BlockPos pos = ((BlockRayTraceResult) result).getPos();
                BlockState state = world.getBlockState(pos);
                beam.remove();

                if (state.isSolid() || state.getMaterial() == Material.WATER
                    && world.isAirBlock(pos.up(1))
                    && world.isAirBlock(pos.up(2))
                ) {
                    player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D);
                    didCast.accept(true);
                    return;
                }
            }
            didCast.accept(false);
        });
    }
}