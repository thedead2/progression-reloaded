package de.thedead2.progression_reloaded.gui.buttons;

import de.thedead2.progression_reloaded.helpers.SplitHelper;
import de.thedead2.progression_reloaded.network.PacketChangeTeam;
import de.thedead2.progression_reloaded.network.PacketHandler;

import static de.thedead2.progression_reloaded.gui.core.GuiList.TOOLTIP;
import static de.thedead2.progression_reloaded.player.PlayerSavedData.TeamAction.LEAVE;
import static net.minecraft.util.text.TextFormatting.BOLD;

public class ButtonLeaveTeam extends ButtonBaseTeam {
    public ButtonLeaveTeam(String text, int x, int y) {
        super(text, x, y);
    }

    @Override
    public void onClicked() {
        PacketHandler.sendToServer(new PacketChangeTeam(LEAVE));
    }

    @Override
    public void addTooltip() {
        TOOLTIP.add(BOLD + "Leave Team");
        TOOLTIP.add(SplitHelper.splitTooltip("Clicking this button will make you leave your current team and return to single player", 40));
    }
}
