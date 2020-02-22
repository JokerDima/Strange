package svenhjol.strange.ambience.client;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.function.Predicate;

public class LongSound extends TickableSound
{
    private final ClientPlayerEntity player;
    private int longTicks;
    private Predicate<ClientPlayerEntity> predicate;
    private float maxVolume;

    public LongSound(ClientPlayerEntity player, SoundEvent sound, float volume, Predicate<ClientPlayerEntity> predicate)
    {
        super(sound, SoundCategory.AMBIENT);
        this.maxVolume = volume;
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.01F;
        this.priority = true;
        this.global = true;
        this.predicate = predicate;
        this.longTicks = -50;
    }

    @Override
    public void tick()
    {
        if (this.player.isAlive()) {

            if (predicate.test(this.player)) {
                ++this.longTicks;
            } else {
                this.longTicks -= 1;
            }

            this.longTicks = Math.min(this.longTicks, 140);
            this.volume = Math.max(0.0F, Math.min((float)this.longTicks / 140, 1.0F)) * maxVolume;

            if (!this.donePlaying && this.volume == 0.0F && this.longTicks < -100)
                this.donePlaying = true;

        } else {
            this.donePlaying = true;
        }
    }
}
