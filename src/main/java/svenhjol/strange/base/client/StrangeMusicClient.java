package svenhjol.strange.base.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.charm.Charm;
import svenhjol.charm.tweaks.client.AmbientMusicClient;
import svenhjol.charm.tweaks.module.AmbientMusicImprovements;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.strange.base.StrangeSounds;

public class StrangeMusicClient
{
    public StrangeMusicClient()
    {
        if (!Charm.hasModule(AmbientMusicImprovements.class)) return;

        // play Þarna in overworld anywhere
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_THARNA, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null || player.world == null) return false;
            return player.world.rand.nextFloat() < 0.08F
                && player.world.getDimension().getType() == DimensionType.OVERWORLD;
        }));

        // play Steinn in overworld underground
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_STEINN, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null || player.world == null) return false;
            return player.getPosition().getY() < 48
                && player.world.getDimension().getType() == DimensionType.OVERWORLD
                && player.world.rand.nextFloat() < 0.1F;
        }));

        // play Mús in cold environments
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_MUS, 1200, 3600, mc ->
            mc.player != null
                && mc.player.world.getBiome(new BlockPos(mc.player)).getCategory() == Biome.Category.ICY
                && mc.player.world.rand.nextFloat() < 0.28F
        ));

        // play Undir in nether underground
        AmbientMusicClient.conditions.add(new AmbientMusicClient.AmbientMusicCondition(StrangeSounds.MUSIC_UNDIR, 1200, 3600, mc -> {
            PlayerEntity player = ClientHelper.getClientPlayer();
            if (player == null) return false;
            return player.getPosition().getY() < 48
                && player.world.getDimension().getType() == DimensionType.THE_NETHER
                && player.world.rand.nextFloat() < 0.33F;
        }));
    }
}
