package svenhjol.strange.totems.module;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.TotemHelper;
import svenhjol.strange.totems.client.TotemOfFlyingClient;
import svenhjol.strange.totems.iface.ITreasureTotem;
import svenhjol.strange.totems.item.TotemOfFlyingItem;
import svenhjol.strange.totems.message.ClientTotemUpdateFlying;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfFlying extends MesonModule implements ITreasureTotem {
    public static TotemOfFlyingItem item;
    public static List<UUID> flyingPlayers = new ArrayList<>();

    @Config(name = "Durability", description = "Durability of the totem. The totem takes a point of damage every time you take off.")
    public static int durability = 32;

    @Config(name = "XP amount", description = "Amount of XP consumed while flying.")
    public static int xpAmount = 1;

    @Config(name = "XP interval", description = "Number of seconds of flying before some XP is consumed.")
    public static double xpInterval = 1.0D;

    @OnlyIn(Dist.CLIENT)
    public static TotemOfFlyingClient client;

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void init() {
        item = new TotemOfFlyingItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureTotems.availableTotems.add(this);
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        client = new TotemOfFlyingClient();
    }

    @SubscribeEvent
    public void onJump(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity
            && (event.getEntityLiving().getHeldItemMainhand().getItem() == item
            || event.getEntityLiving().getHeldItemOffhand().getItem() == item)
        ) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            ItemStack held = null;

            if (player.experienceTotal <= 0) return;

            for (Hand hand : Hand.values()) {
                if (player.getHeldItem(hand).getItem() == item) {
                    held = player.getHeldItem(hand);
                }
            }
            if (held == null) return;

            TotemHelper.damageOrDestroy(player, held, 1);
            if (player.world.isRemote) {
                client.effectStartFlying(player);
            }
            enableFlight(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START
            && event.player != null
            && event.player.world.getGameTime() % 4 == 0
        ) {
            PlayerEntity player = event.player;
            World world = player.world;

            if (player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item) {
                if (player.abilities.isFlying) {
                    if (!world.isRemote && world.getGameTime() % (xpInterval * 20) == 0) {
                        int xp = player.experienceTotal;
                        if (xp <= 0) {
                            disableFlight(player);
                            Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientTotemUpdateFlying(ClientTotemUpdateFlying.DISABLE), (ServerPlayerEntity) player);
                            return;
                        }

                        for (Hand hand : Hand.values()) {
                            ItemStack held = player.getHeldItem(hand);
                            if (held.getItem() != item) continue;
                            player.giveExperiencePoints(-xpAmount);
                        }
                    }
                    if (world.isRemote) {
                        client.effectFlying(player);
                    }
                }
                return;
            }

            if (flyingPlayers.contains(player.getUniqueID())) {
                disableFlight(player);
            }
        }
    }

    private void disableFlight(PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            player.abilities.allowFlying = true;
        } else {
            player.abilities.isFlying = false;
            player.abilities.allowFlying = false;
            flyingPlayers.remove(player.getUniqueID());
        }
    }

    private void enableFlight(PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) {
            player.abilities.allowFlying = true;
        } else {
            player.abilities.allowFlying = true;
            player.abilities.isFlying = true;
            flyingPlayers.add(player.getUniqueID());
        }
    }

    @Override
    public ItemStack getTreasureItem() {
        return new ItemStack(item);
    }
}
