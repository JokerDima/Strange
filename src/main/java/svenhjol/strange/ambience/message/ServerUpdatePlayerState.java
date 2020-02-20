package svenhjol.strange.ambience.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.ruins.module.UndergroundRuins;
import svenhjol.strange.ruins.module.Vaults;

import java.util.function.Supplier;

/**
 * Server assembles list of state for client, like inside structures, is day or night...
 */
public class ServerUpdatePlayerState implements IMesonMessage
{
    public ServerUpdatePlayerState()
    {
    }

    public static void encode(ServerUpdatePlayerState msg, PacketBuffer buf)
    {
    }

    public static ServerUpdatePlayerState decode(PacketBuffer buf)
    {
        return new ServerUpdatePlayerState();
    }

    public static class Handler
    {
        public static void handle(final ServerUpdatePlayerState msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                ServerWorld world = player.getServerWorld();
                BlockPos pos = player.getPosition();

                CompoundNBT nbt = new CompoundNBT();
                nbt.putBoolean("mineshaft", Feature.MINESHAFT.isPositionInsideStructure(world, pos));
                nbt.putBoolean("stronghold", Feature.STRONGHOLD.isPositionInsideStructure(world, pos));
                nbt.putBoolean("fortress", Feature.NETHER_BRIDGE.isPositionInsideStructure(world, pos));
                nbt.putBoolean("shipwreck", Feature.SHIPWRECK.isPositionInsideStructure(world, pos));
                nbt.putBoolean("village", Feature.VILLAGE.isPositionInsideStructure(world, pos));
                nbt.putBoolean("day", world.isDaytime());

                if (Strange.hasModule(UndergroundRuins.class)) {
                    nbt.putBoolean("underground_ruin", UndergroundRuins.structure.isPositionInsideStructure(world, pos));
                }
                if (Strange.hasModule(Vaults.class)) {
                    nbt.putBoolean("vaults", Vaults.structure.isPositionInsideStructure(world, pos));
                }
                if (StrangeLoader.quarkCompat != null && StrangeLoader.quarkCompat.hasModule(new ResourceLocation("quark:big_dungeons"))) {
                    nbt.putBoolean("big_dungeon", StrangeLoader.quarkCompat.isInsideBigDungeon(world, pos));
                }

                PacketHandler.sendTo(new ClientUpdatePlayerState(nbt), player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}