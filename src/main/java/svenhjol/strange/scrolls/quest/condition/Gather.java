package svenhjol.strange.scrolls.quest.condition;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.base.QuestHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Gather implements IDelegate
{
    public final static String ID = "Gather";

    private IQuest quest;
    private ItemStack stack;
    private int count;
    private int collected;

    private final String STACK = "stack";
    private final String COUNT = "count";
    private final String COLLECTED = "collected";

    @Override
    public String getType()
    {
        return Criteria.ACTION;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean respondTo(Event event)
    {
        if (isSatisfied()) return false;
        if (collected >= count) return false;

        if (event instanceof QuestEvent.Accept) {
            // gather as many things as possible from the player's inventory
            final QuestEvent.Accept qe = (QuestEvent.Accept)event;
            final PlayerEntity player = qe.getPlayer();
            final ImmutableList<NonNullList<ItemStack>> inventories = PlayerHelper.getInventories(player);
            final Item requiredItem = stack.getItem();

            for (NonNullList<ItemStack> inventory : inventories) {
                for (ItemStack invStack : inventory) {
                    if (invStack.isEmpty()) continue;
                    if (invStack.getItem() != requiredItem) continue;
                    if (isSatisfied()) continue;

                    int invCount = invStack.getCount();
                    int shrinkBy = Math.min(getRemaining(), invCount);
                    this.collected += shrinkBy;
                    invStack.shrink(shrinkBy);
                }
            }

            showProgress(player);
        }

        if (event instanceof EntityItemPickupEvent) {
            EntityItemPickupEvent qe = (EntityItemPickupEvent)event;
            ItemStack pickedUp = qe.getItem().getItem();

            if (this.stack == null || pickedUp.getItem() != stack.getItem().getItem()) return false;

            PlayerEntity player = qe.getPlayer();
            World world = qe.getPlayer().world;

            int pickedUpCount = pickedUp.getCount();
            int remaining = getRemaining();

            if (pickedUpCount > remaining || remaining - 1 == 0) {
                // set the count to the remainder
                pickedUp.setCount(pickedUpCount - remaining);
                pickedUpCount = remaining;
            } else {
                // cancel the event, don't pick up any items
                qe.getItem().remove();
                qe.setResult(Event.Result.DENY);
                qe.setCanceled(true);
            }

            collected += pickedUpCount;
            showProgress(player);

            return true;
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count <= collected;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        int collected = Math.min(this.collected, this.count);
        if (collected == 0 || count == 0) return 0;
        return ((float)collected / (float)count) * 100;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put(STACK, stack.serializeNBT());
        tag.putInt(COLLECTED, collected);
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
        this.collected = data.getInt(COLLECTED);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public Gather setCount(int count)
    {
        this.count = count;
        return this;
    }

    public Gather setStack(ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    public int getCollected()
    {
        return this.collected;
    }

    public int getCount()
    {
        return this.count;
    }

    public int getRemaining()
    {
        return count - collected;
    }

    public ItemStack getStack()
    {
        return this.stack;
    }

    private void showProgress(PlayerEntity player)
    {
        if (isSatisfied()) {
            QuestHelper.effectCompleted(player, new TranslationTextComponent("event.strange.quests.gathered_all"));
        } else {
            QuestHelper.effectCounted(player);
        }
    }
}
