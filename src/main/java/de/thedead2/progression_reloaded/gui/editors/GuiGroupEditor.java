package de.thedead2.progression_reloaded.gui.editors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import de.thedead2.progression_reloaded.gui.buttons.ButtonJoinTeam;
import de.thedead2.progression_reloaded.gui.buttons.ButtonLeaveTeam;
import de.thedead2.progression_reloaded.gui.buttons.ButtonNewTeam;
import de.thedead2.progression_reloaded.gui.core.GuiList;
import de.thedead2.progression_reloaded.gui.core.IBarProvider;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.lib.PInfo;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketInvitePlayer;
import de.thedead2.progression_reloaded.network.PacketSyncUsernameCache;
import de.thedead2.progression_reloaded.player.PlayerTeam;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import static de.thedead2.progression_reloaded.gui.core.GuiList.*;
import static net.minecraft.util.text.TextFormatting.BOLD;
import static net.minecraft.util.text.TextFormatting.ITALIC;

public class GuiGroupEditor extends GuiBaseEditor implements IBarProvider, ITextEditable {
    private static Set<Invite> invites = new LinkedHashSet<Invite>();
    private static Cache<PlayerTeam, Set<AbstractClientPlayer>> playerList;
    private boolean isPopup;
    private String username;

    public GuiGroupEditor() {
        features.add(BACKGROUND);
        features.add(GROUP_BG);
    }

    public void addInvite(UUID owner, String name) {
        invites.add(new Invite(owner, name));
        CORE.initGui(); //Reload
    }

    public void removeInvite(Invite invite) {
        invites.remove(invite);
        CORE.initGui();
    }

    @Override
    public boolean hasButtons() {
        return true;
    }

    @Override
    public IEditorMode getPreviousGui() {
        return null;
    }

    @Override
    public Object getKey() {
        return this;
    }

    public void clear() {
        playerList = CacheBuilder.newBuilder().maximumSize(64).build(); //Reset the cache when we reload the screen
    }

    @Override
    public void initData() {
        CORE.scrollingEnabled = true;
        List<GuiButton> buttons = CORE.getButtonNewList();
        buttons.add(new ButtonNewTeam("New Team", 5, CORE.screenTop + 25));
        buttons.add(new ButtonLeaveTeam("Leave Team", 75, CORE.screenTop + 25));
        int x = 0;
        for (Invite invite: invites) {
            buttons.add(new ButtonJoinTeam(invite, 160 + x, CORE.screenTop + 25));
            x += 30;
        }

        GROUP_BG.setProvider(this);
    }

    private boolean clickLeft(int screenCentre, int mouseX, int mouseY) {
        PlayerTeam team = PlayerTracker.getClientPlayer().getTeam();
        if (!isPopup && mouseY >= 90 && mouseY < 100) {
            return TEXT_EDITOR_SIMPLE.setEditable(team);
        }

        if (!team.isSingle() && team.isOwner(MCClientHelper.getPlayer())) {
            int xPos = getPlayers(team).size() * 40;
            if (mouseY >= 100 && mouseY < 110 && !isPopup) {
                team.toggleMultiple();
            } else if (mouseY >= 110 && mouseY < 120 && !isPopup) {
                team.toggleIsTrueTeam();
            } else if (mouseX >= 10 + xPos + CORE.getOffsetX() && mouseX <= 65 + xPos + CORE.getOffsetX() && mouseY >= 170 && mouseY <= 225) {
                username = ""; //Reset the username everytime you click this
                if (isPopup) {
                    //If we had the popup and clicked this button
                } else TEXT_EDITOR_SIMPLE.setEditable(this);

                isPopup = !isPopup;
                return true;
            } else {
                TEXT_EDITOR_SIMPLE.clearEditable();
                isPopup = false;
            }
        }

        return false;
    }

    private void drawLeft(int screenCentre, int mouseX, int mouseY) {
        drawGradientRectWithBorder(-2, 50, CORE.screenWidth + 4, 50 + 15, 0xFF6C00D9, 0xFF330066, 0xFF330066);
        drawText("Team Info", 10, 54, 0xFFFFFFFF);

        PlayerTeam team = PlayerTracker.getClientPlayer().getTeam();
        drawText("Team Type:", 5, 70, 0xFFFFFFFF);
        drawText(team.getType().name(), 105, 70, 0xFFFFFFFF);

        drawText("Team Owner:", 5, 80, 0xFFFFFFFF);
        drawText(getUsernameFromID(team.getOwner()), 105, 80, 0xFFFFFFFF);

        drawText("Team Name:", 5, 90, 0xFFFFFFFF);
        drawText(GuiList.TEXT_EDITOR_SIMPLE.getText(team), 105, 90, 0xFFFFFFFF);

        if (isPopup) {
            CORE.drawRectWithBorder(100, 86, 300, 86 + 15, 0xFF000000, 0xFFFFFFFF);
            String display = username.equals("") ? "Enter a Username..." : GuiList.TEXT_EDITOR_SIMPLE.getText(this);
            drawText(display, 110, 90, 0xFFFFFFFF);
        }

        if (!team.isSingle()) {
            drawText("Multiple Rewards:", 5, 100, 0xFFFFFFFF);
            drawText("" + team.giveMultipleRewards(), 105, 100, 0xFFFFFFFF);
            if (!isPopup && mouseY >= 100 && mouseY < 110) {
                addTooltip(BOLD + "Multiple Rewards");
                addTooltip("If this is true, team members will be able to claim their own rewards where applicable\n\n" + ITALIC + "Click to Toggle", 45);
            }

            drawText("True Team:", 5, 110, 0xFFFFFFFF);
            drawText("" + team.isTrueTeam(), 105, 110, 0xFFFFFFFF);
            if (!isPopup && mouseY >= 110 && mouseY < 120) {
                addTooltip(BOLD + "True Team");
                addTooltip("If this is true, then tasks that require you to have items in your inventory will count the items from all the team members instead of checking for them on and individual basis\n\n" + ITALIC + "Click to Toggle", 50);
            }
        }

        if (!isPopup && mouseY >= 90 && mouseY < 100) {
            if (team.isOwner(MCClientHelper.getPlayer())) {
                addTooltip(BOLD + "Click to edit name");
            } else addTooltip(BOLD + "Only owner can rename the team");
        }

        drawGradientRectWithBorder(-2, 141, CORE.screenWidth + 4, 141 + 15, 0xFF6C00D9, 0xFF330066, 0xFF330066);
        drawText("Team Members", 10, 145, 0xFFFFFFFF);
        try {
            int xPos = 0;
            for (EntityPlayer player : getPlayers(team)) {
                GuiInventory.drawEntityOnScreen(20 + xPos + CORE.getOffsetX(), 230 + CORE.screenTop, 35, 5, 10, player);
                if (mouseX >= xPos + CORE.getOffsetX() && mouseX <= xPos + 39 + CORE.getOffsetX() && mouseY >= 164 && mouseY <= 231) {
                    addTooltip(player.getDisplayNameString());
                }

                xPos += 40;
            }

            if (!team.isSingle() && team.isOwner(MCClientHelper.getPlayer())) {
                int crossY = 64;
                if (mouseX >= 10 + CORE.getOffsetX() + xPos && mouseX <= 65 + CORE.getOffsetX() + xPos && mouseY >= 170 && mouseY <= 225) {
                    addTooltip(BOLD + "Add New Member");
                    addTooltip("Click this to open a window to invite a new member, once the window is open and you have typed who you want to invite, press enter to send the invite", 40);
                    crossY = 119;
                }

                GlStateManager.enableBlend();
                int color = 0xFF6C00D9;
                float red = (color >> 16 & 255) / 255.0F;
                float green = (color >> 8 & 255) / 255.0F;
                float blue = (color & 255) / 255.0F;
                GlStateManager.color(red, green, blue, 1F);
                CORE.drawTexture(PInfo.textures, xPos + 10 + CORE.getOffsetX(), 170, 201, crossY, 55, 55);
            }
        } catch (Exception e) {}
    }

    private boolean clickRight(int screenCentre, int mouseX, int mouseY) {
        return false;
    }

    private void drawRight(int screenCentre, int mouseX, int mouseY) {
        if (isPopup) {

        }

        //drawGradientRectWithBorder(screenCentre + 2, 90, core.screenWidth - 2, 105, 0xFFCCCCCC, THEME.conditionEditorGradient1, 0xFF000000);
        //text = "Current Invites";
        //drawText(text, screenCentre + 12, 59, 0xFFFFFFFF);
    }

    @Override
    public void drawGuiForeground(boolean overlayvisible, int mouseX, int mouseY) {
        int screenCentre = CORE.screenWidth / 2;
        drawLeft(screenCentre, mouseX, mouseY);
        drawRight(screenCentre, mouseX, mouseY);
    }

    public static String getUsernameFromID(UUID uuid) {
        String name = PacketSyncUsernameCache.cache.get(uuid);
        if (name == null || name.equals("")) name = "Unknown";
        return name;
    }

    public Set<AbstractClientPlayer> getPlayers(final PlayerTeam team) {
        try {
            return playerList.get(team, new Callable<Set<AbstractClientPlayer>>() {
                @Override
                public Set<AbstractClientPlayer> call() throws Exception {
                    Set<AbstractClientPlayer> players = new LinkedHashSet();
                    Minecraft mc = Minecraft.getMinecraft();
                    players.add(new EntityOtherPlayerMP(mc.theWorld, new GameProfile(team.getOwner(), getUsernameFromID(team.getOwner()))));
                    if (!team.isOwner(mc.thePlayer)) players.add(mc.thePlayer);
                    for (UUID uuid : team.getMembers()) {
                        if (uuid.equals(PlayerHelper.getUUIDForPlayer(mc.thePlayer))) continue;
                        else {
                            players.add(new EntityOtherPlayerMP(mc.theWorld, new GameProfile(uuid, getUsernameFromID(uuid))));
                        }
                    }

                    return players;
                }
            });
        } catch (Exception e) {
            return new LinkedHashSet();
        }
    }

    @Override
    public boolean guiMouseClicked(boolean overlayvisible, int mouseX, int mouseY, int button) {
        int screenCentre = CORE.screenWidth / 2;
        PlayerTeam team = PlayerTracker.getClientPlayer().getTeam();
        if (team.isOwner(MCClientHelper.getPlayer()) && clickLeft(screenCentre, mouseX, mouseY)) return true;
        else if (clickRight(screenCentre, mouseX, mouseY)) return true;
        else return false;
    }

    @Override
    public int getColorForBar(BarColorType type) {
        switch (type) {
            case BAR1_GRADIENT1:
                return 0xFF6C00D9;
            case BAR1_GRADIENT2:
                return 0xFF330066;
            case BAR1_BORDER:
                return 0xFF330066;
            case BAR1_FONT:
                return THEME.conditionEditorFont;
            case BAR1_UNDERLINE:
                return 0x00330066;
            default:
                return 0;
        }
    }

    @Override
    public String getTextField() {
        return username;
    }

    @Override
    public void setTextField(String text) {
        username = text.replace("\n", "");
        if (text.contains("\n")) {
            PlayerTeam team = PlayerTracker.getClientPlayer().getTeam();
            PacketHandler.sendToServer(new PacketInvitePlayer(team.getOwner(), team.getName(), username));
            isPopup = false;
            username = "";
        }
    }

    public static class Invite {
        public UUID owner;
        public String name;

        public Invite(UUID owner, String name) {
            this.owner = owner;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return owner.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Invite) {
                return ((Invite)object).owner.equals(owner);
            }

            return false;
        }
    }
}