package svenhjol.strange.scrolls.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.capability.DummyCapability;
import svenhjol.strange.scrolls.capability.IQuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsCapability;
import svenhjol.strange.scrolls.capability.QuestsStorage;
import svenhjol.strange.scrolls.event.QuestEvents;
import svenhjol.strange.scrolls.message.RequestCurrentQuests;
import svenhjol.strange.scrolls.message.SendCurrentQuests;
import svenhjol.strange.scrolls.quest.GatheringHandler;
import svenhjol.strange.scrolls.quest.HuntingHandler;
import svenhjol.strange.scrolls.quest.IQuestHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Quests extends MesonModule
{
    public static List<IQuestHandler> handlers = new ArrayList<>();

    @Config(name = "Maximum quests", description = "Maximum number of quests a player can do at once.")
    public static int max = 3;

    @CapabilityInject(IQuestsCapability.class)
    public static Capability<IQuestsCapability> QUESTS = null;

    public static ResourceLocation QUESTS_CAP_ID = new ResourceLocation(Strange.MOD_ID, "quest_capability");

    @Override
    public void init()
    {
        handlers.add(new GatheringHandler());
        handlers.add(new HuntingHandler());

        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, RequestCurrentQuests.class, RequestCurrentQuests::encode, RequestCurrentQuests::decode, RequestCurrentQuests.Handler::handle);
        PacketHandler.HANDLER.registerMessage(PacketHandler.index++, SendCurrentQuests.class, SendCurrentQuests::encode, SendCurrentQuests::decode, SendCurrentQuests.Handler::handle);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        CapabilityManager.INSTANCE.register(IQuestsCapability.class, new QuestsStorage(), QuestsCapability::new); // register the interface, storage, and implementation
        MinecraftForge.EVENT_BUS.register(new QuestEvents()); // add all quest-related events to the bus
    }

    public static IQuestsCapability getCapability(PlayerEntity player)
    {
        return player.getCapability(QUESTS, null).orElse(new DummyCapability());
    }

    public static void updateQuests(PlayerEntity player)
    {
        IQuestsCapability cap = Quests.getCapability(player);
        PacketHandler.sendTo(new SendCurrentQuests(cap.getCurrentQuests(player)), (ServerPlayerEntity)player);
    }

    public enum QuestType implements IMesonEnum
    {
        Gathering,
        Hunting,
        Raiding,
        Exploring;

        // TODO might be possible to move this to Meson?
        public static QuestType random()
        {
            List<QuestType> vals = Arrays.asList(values());
            return vals.get(new Random().nextInt(vals.size()));
        }
    }
}
