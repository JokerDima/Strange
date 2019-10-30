package svenhjol.strange.scrolls.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import svenhjol.strange.scrolls.quest.panel.ActionsPanel;
import svenhjol.strange.scrolls.quest.panel.LimitsPanel;
import svenhjol.strange.scrolls.quest.panel.RewardsPanel;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;

public class QuestScreen extends Screen implements IRenderable
{
    private PlayerEntity player;
    private ItemStack stack;
    private IQuest quest;

    public QuestScreen(PlayerEntity player, ItemStack stack)
    {
        super(new StringTextComponent(ScrollItem.getQuest(stack).getTitle()));
        this.player = player;
        this.stack = stack;
        this.quest = ScrollItem.getQuest(stack.copy());
    }

    public QuestScreen(PlayerEntity player, IQuest quest)
    {
        super(new StringTextComponent(quest.getTitle()));
        this.player = player;
        this.quest = quest;
        this.stack = null;
    }

    @Override
    protected void init()
    {
        super.init();
        renderButtons();
    }

    @Override
    public void onClose()
    {
        this.close();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        int mid = (width / 2);

        if (this.minecraft == null || this.minecraft.world == null) return;

        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), mid, 18, 0xFFFFFF);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();

        this.drawCenteredString(this.font, quest.getDescription(), mid, 36, 0xFFFFFF);

        Criteria criteria = quest.getCriteria();
        final List<Condition<IDelegate>> actions = criteria.getConditions(Criteria.ACTION);
        final List<Condition<IDelegate>> limits = criteria.getConditions(Criteria.LIMIT);
        final List<Condition<IDelegate>> rewards = criteria.getConditions(Criteria.REWARD);

        List<String> actionIds = new ArrayList<>();
        for (Condition<IDelegate> action : actions) {
            if (!actionIds.contains(action.getId())) actionIds.add(action.getId());
        }

        int panelY = 64;
        int panelMid;
        int panelWidth;

        for (int i = 0; i < actionIds.size(); i++) {
            int ii = (i * 2) + 1;
            panelMid = (ii * (width / (actionIds.size() * 2)));
            panelWidth = width / ii;
            new ActionsPanel(quest, actionIds.get(i), panelMid, panelY, panelWidth);
        }

        boolean splitLimitsAndRewards = false;
        if (limits.size() > 0 && rewards.size() > 0) {
            splitLimitsAndRewards = true;
        }

        if (splitLimitsAndRewards) {
            new LimitsPanel(quest, width / 4, 145, 85);
            new RewardsPanel(quest, 3 * (width / 4), 145, 85);
        }

        GlStateManager.popMatrix();
        super.render(mouseX, mouseY, partialTicks);
    }

    public void renderButtons()
    {
        int y = (height / 4) + 160;
        int w = 80;
        int h = 20;

        if (stack != null) {
            this.addButton(new Button((width / 2) - 140, y, w, h, I18n.format("gui.strange.scrolls.decline"), (button) -> this.decline()));
            this.addButton(new Button((width / 2) - 40, y, w, h, I18n.format("gui.strange.scrolls.close"), (button) -> this.close()));
            this.addButton(new Button((width / 2) + 60, y, w, h, I18n.format("gui.strange.scrolls.accept"), (button) -> this.accept()));
        } else {
            this.addButton(new Button((width / 2) - 100, y, w, h, I18n.format("gui.strange.scrolls.quit"), (button) -> this.decline()));
            this.addButton(new Button((width / 2) + 20, y, w, h, I18n.format("gui.strange.scrolls.close"), (button) -> this.close()));
        }
    }

    private void accept()
    {
        if (stack != null) {
            MinecraftForge.EVENT_BUS.post(new QuestEvent.Accept(player, quest));
            stack.shrink(1);
        }
        this.close();
    }

    private void decline()
    {
        if (stack != null) {
            stack.shrink(1);
        } else {
            MinecraftForge.EVENT_BUS.post(new QuestEvent.Decline(player, quest));
        }
        this.close();
    }

    private void close()
    {
        if (this.minecraft != null) {
            this.minecraft.displayGuiScreen(null);
        }
    }
}
