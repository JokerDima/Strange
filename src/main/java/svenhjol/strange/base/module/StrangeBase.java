package svenhjol.strange.base.module;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.ambience.message.ServerUpdatePlayerState;
import svenhjol.strange.base.StrangeCategories;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.BASE, hasSubscriptions = true)
public class StrangeBase extends MesonModule
{
    @Config(name = "Client/Server update ticks", description = "Heartbeat interval to synchronise additional world state to the client.")
    public static int clientServerUpdateTicks = 200;

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event)
    {
        if (event.player.world.isRemote
            && event.player.world.getGameTime() % 120 == 0
        ) {
            PacketHandler.sendToServer(new ServerUpdatePlayerState());
        }
    }
}