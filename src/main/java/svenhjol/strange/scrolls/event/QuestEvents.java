package svenhjol.strange.scrolls.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsProvider;
import svenhjol.strange.scrolls.message.ClientQuestAction;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuest.State;

import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("unused")
public class QuestEvents
{
    @SubscribeEvent
    public void onQuestAccept(QuestEvent.Accept event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        quest.setState(State.Started);
        Quests.getCapability(player).acceptQuest(player, quest);
        PacketHandler.sendTo(new ClientQuestAction(ClientQuestAction.ACCEPTED, quest), (ServerPlayerEntity)player);

        respondToEvent(player, event);
    }

    @SubscribeEvent
    public void onQuestComplete(QuestEvent.Complete event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        respondToEvent(player, event);
        Quests.getCapability(player).removeQuest(player, quest);
        PacketHandler.sendTo(new ClientQuestAction(ClientQuestAction.COMPLETED, quest), (ServerPlayerEntity)player);
    }

    @SubscribeEvent
    public void onQuestDecline(QuestEvent.Decline event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        respondToEvent(player, event);
        Quests.getCapability(player).removeQuest(player, quest);
        PacketHandler.sendTo(new ClientQuestAction(ClientQuestAction.DECLINED, quest), (ServerPlayerEntity)player);
    }

    @SubscribeEvent
    public void onQuestFail(QuestEvent.Fail event)
    {
        final PlayerEntity player = event.getPlayer();
        final IQuest quest = event.getQuest();

        respondToEvent(player, event);
        Quests.getCapability(player).removeQuest(player, quest);
        PacketHandler.sendTo(new ClientQuestAction(ClientQuestAction.FAILED, quest), (ServerPlayerEntity)player);
    }

    @SubscribeEvent
    public void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof PlayerEntity)) return;
        event.addCapability(Quests.QUESTS_CAP_ID, new QuestsProvider()); // Attach cap and provider to Forge's player capabilities. Provider has the implementation.
    }

    @SubscribeEvent
    public void onPlayerSave(PlayerEvent.SaveToFile event)
    {
        final PlayerEntity player = event.getPlayer();

        player.getPersistentData().put(
            Quests.QUESTS_CAP_ID.toString(),
            Quests.getCapability(player).writeNBT());
    }

    @SubscribeEvent
    public void onPlayerLoad(PlayerEvent.LoadFromFile event)
    {
        final PlayerEntity player = event.getPlayer();

        Quests.getCapability(player).readNBT(
            player.getPersistentData()
                .get(Quests.QUESTS_CAP_ID.toString()));
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath()) return;
        IQuestsCapability oldCap = Quests.getCapability(event.getOriginal());
        IQuestsCapability newCap = Quests.getCapability(event.getPlayer());
        newCap.readNBT(oldCap.writeNBT());

        PlayerEntity player = event.getPlayer();
        if (player != null) {
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event)
    {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(ItemCraftedEvent event)
    {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof PlayerEntity)
            && event.getSource().getTrueSource() instanceof PlayerEntity
            && event.getEntityLiving() != null
        ) {
            PlayerEntity player = (PlayerEntity)event.getSource().getTrueSource();
            respondToEvent(player, event);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END
            && event.player != null
            && event.player.world.getGameTime() % 10 == 0
            && event.player.isAlive()
        ) {
            respondToEvent(event.player, event);
        }
    }

    private void respondToEvent(PlayerEntity player, Event event)
    {
        if (player == null || !player.isAlive()) return;
        boolean responded = false;

        ConcurrentLinkedDeque<IQuest> quests = new ConcurrentLinkedDeque<>(Quests.getCurrent(player));

        for (IQuest q : quests) {
            responded = q.respondTo(event) || responded;
        }

        if (responded) Quests.update(player);
    }
}
