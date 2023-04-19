package de.thedead2.progression_reloaded.gui.buttons;

import de.thedead2.progression_reloaded.gui.editors.GuiGroupEditor.Invite;
import de.thedead2.progression_reloaded.helpers.SplitHelper;
import de.thedead2.progression_reloaded.network.PacketChangeTeam;
import de.thedead2.progression_reloaded.network.PacketHandler;
import net.minecraft.client.gui.GuiScreen;

import static de.thedead2.progression_reloaded.gui.core.GuiList.GROUP_EDITOR;
import static de.thedead2.progression_reloaded.gui.core.GuiList.TOOLTIP;
import static de.thedead2.progression_reloaded.player.PlayerSavedData.TeamAction.JOIN;
import static net.minecraft.util.text.TextFormatting.BOLD;

public class ButtonJoinTeam extends ButtonBaseTeam {
    private Invite invite;

    public ButtonJoinTeam(Invite invite, int x, int y) {
        super("Join " + invite.name, x, y);
        this.invite = invite;
    }

    @Override
    public void onClicked() {
        if (!GuiScreen.isShiftKeyDown()) PacketHandler.sendToServer(new PacketChangeTeam(JOIN, invite.owner));

        GROUP_EDITOR.removeInvite(invite); //Remove the invite always
    }

    @Override
    public void addTooltip() {
        TOOLTIP.add(BOLD + "Join Team");
        TOOLTIP.add(SplitHelper.splitTooltip("If you want to join this team, click, if not shift click.", 40));
    }
}
