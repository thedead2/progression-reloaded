package de.thedead2.progression_reloaded.gui.buttons;

import de.thedead2.progression_reloaded.helpers.SplitHelper;
import de.thedead2.progression_reloaded.network.PacketChangeTeam;
import de.thedead2.progression_reloaded.network.PacketHandler;

import static de.thedead2.progression_reloaded.gui.core.GuiList.TOOLTIP;
import static de.thedead2.progression_reloaded.player.PlayerSavedData.TeamAction.NEW;
import static net.minecraft.util.text.TextFormatting.BOLD;

public class ButtonNewTeam extends ButtonBaseTeam {
    public ButtonNewTeam(String text, int x, int y) {
        super(text, x, y);
    }

    @Override
    public void onClicked() {
        PacketHandler.sendToServer(new PacketChangeTeam(NEW));
    }

    @Override
    public void addTooltip() {
        TOOLTIP.add(BOLD + "New Team");
        TOOLTIP.add(SplitHelper.splitTooltip("Clicking this button will create a new team, with you as the owner", 40));
    }
}
