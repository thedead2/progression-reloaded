package de.thedead2.progression_reloaded.client.gui.components.toasts;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.PRKeyMappings;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientOnProgressChangedPacket;
import de.thedead2.progression_reloaded.network.packets.ServerFollowQuestPacket;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;


public class ProgressToast extends NotificationToast {

    private final ClientOnProgressChangedPacket.Type type;
    private final DrawableTexture toastTexture;
    private final TextBox textBox;
    private final IDisplayInfo<?> displayInfo;

    private final ResourceLocation font;


    public ProgressToast(Area area, IDisplayInfo<?> displayInfo, ClientOnProgressChangedPacket.Type type, TextureInfo toastTexture, ResourceLocation font) {
        super(area, type.getTitle(), Alignment.CENTERED, new SimpleAnimation(0, MathHelper.secondsToTicks(1), LoopTypes.LOOP_TIMES_INVERSE(1), AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC, false));
        this.type = type;
        this.font = font;
        this.toastTexture = new DrawableTexture(toastTexture, this.area);
        int color = Color.WHITE.getRGB();
        if(type == ClientOnProgressChangedPacket.Type.QUEST_FAILED) {
            color = this.message.getStyle().getColor().getValue();
        }

        this.displayInfo = displayInfo;
        this.textBox = new TextBox(this.area, List.of(
                new FormattedString(this.message, font, FontFormatting.defaultFormatting().setLineHeight(4).setColor(color), true),
                new FormattedString(displayInfo.title(), font, FontFormatting.defaultFormatting().setLineHeight(10).setColor(color).setLetterSpacing(2).setTextAlignment(Alignment.TOP_CENTERED), true)
        ));

        this.alignWithOffset(this.toastAlignment, null, 0, -60);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);

        this.animation.startIfNeeded()
                      .animate(0, 1, this::setAlpha)
                      .sleepIf(IAnimation::isFinishedButLooping, MathHelper.secondsToTicks(4));

        this.toastTexture.render(poseStack, mouseX, mouseY, partialTick);

        ModRenderer renderer = ModClientInstance.getInstance().getModRenderer();

        FormattedString string = FormattedString.EMPTY;

        if((this.type == ClientOnProgressChangedPacket.Type.NEW_QUEST || this.type == ClientOnProgressChangedPacket.Type.QUEST_UPDATED) && !renderer.isQuestFollowed(this.displayInfo.id())) {
            if(PRKeyMappings.FOLLOW_QUEST_KEY.isDown()) {
                PRNetworkHandler.sendToServer(new ServerFollowQuestPacket(this.displayInfo.id()));
            }
            else {
                string = new FormattedString("[" + PRKeyMappings.toString(PRKeyMappings.FOLLOW_QUEST_KEY).toUpperCase() + "] Follow Quest", this.font, FontFormatting.defaultFormatting().setLineHeight(3).setTextAlignment(Alignment.LEFT_CENTERED).setAlpha(this.alpha), true);
            }
        }

        this.textBox.insertText(string);
        this.textBox.render(poseStack, mouseX, mouseY, partialTick);
        this.textBox.deleteText(-string.length());
    }


    @Override
    public ProgressToast setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.toastTexture.setAlpha(alpha);
        this.textBox.setAlpha(alpha);
        return this;
    }

}
