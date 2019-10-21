package svenhjol.strange.scrolls.quest.iface;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.iface.IMesonEnum;
import svenhjol.strange.scrolls.quest.Criteria;

import java.util.UUID;

public interface IQuest
{
    String getId();

    String getDescription();

    String getTitle();

    Criteria getCriteria();

    UUID getSeller();

    int getTier();

    CompoundNBT toNBT();

    boolean respondTo(Event event);

    State getState();

    void fromNBT(CompoundNBT tag);

    void generateId();

    void setId(String id);

    void setTitle(String title);

    void setDescription(String description);

    void setSeller(UUID sellerId);

    void setTier(int tier);

    void setCriteria(Criteria criteria);

    void setState(State state);

    enum State implements IMesonEnum
    {
        NotStarted,
        Started,
        Failed,
        Completed;

        public static State valueOrDefault(String name, State def) {
            try {
                if (name == null || name.isEmpty()) return def;
                return State.valueOf(name);
            } catch (Exception e) {
                return def;
            }
        }
    }
}
