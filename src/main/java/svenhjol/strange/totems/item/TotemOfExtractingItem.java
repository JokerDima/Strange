package svenhjol.strange.totems.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.math.BlockPos;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.totems.module.TotemOfExtracting;

import javax.annotation.Nullable;

public class TotemOfExtractingItem extends MesonItem
{
    private static final String POS = "pos";
    private static final String DIM = "dim";

    public TotemOfExtractingItem(MesonModule module)
    {
        super(module, "totem_of_extracting", new Properties()
            .group(ItemGroup.TRANSPORTATION)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfExtracting.durability)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return getPos(stack) != null;
    }

    @Nullable
    public static BlockPos getPos(ItemStack stack)
    {
        long pos = ItemNBTHelper.getLong(stack, POS, 0);
        return pos == 0 ? null : BlockPos.fromLong(pos);
    }

    public static int getDim(ItemStack stack)
    {
        return ItemNBTHelper.getInt(stack, DIM, 0);
    }

    public static void clearTags(ItemStack stack)
    {
        stack.removeChildTag(POS);
        stack.removeChildTag(DIM);
    }

    public static void setPos(ItemStack stack, BlockPos pos)
    {
        ItemNBTHelper.setLong(stack, POS, pos.toLong());
    }

    public static void setDim(ItemStack stack, int dim)
    {
        ItemNBTHelper.setInt(stack, DIM, dim);
    }
}
