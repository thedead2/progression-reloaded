package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomTooltip;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@ProgressionRule(name="stepAssist", color=0xFF661A00, meta="stepAssist")
public class RewardStepAssist extends RewardBaseAbility implements ICustomTooltip {
    public float steps = 0.5F;

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.stepAssist.description", steps);
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.EDIT ? 100 : 65;
    }

    @Override
    public void addAbilityTooltip(List list) {
        list.add(TextFormatting.GRAY + getProvider().getLocalisedName() + ": " + steps);
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            float step = PlayerTracker.getPlayerData(player).getAbilities().getStepAssist();
            float steps = 0.5F * (step + 1);
            if (steps > player.stepHeight) {
                player.stepHeight = steps;
                player.getEntityData().setBoolean("HasRewardStepAssist", true);
            }

            if (step == 0.5F && player.getEntityData().hasKey("HasRewardStepAssist")) {
                player.getEntityData().removeTag("HasRewardStepAssist");
                player.stepHeight = 0.5F;
            }
        }
    }

    @Override
    public void reward(EntityPlayerMP player) {
        PlayerTracker.getServerPlayer(player).addStepAssist(steps);
    }
}
