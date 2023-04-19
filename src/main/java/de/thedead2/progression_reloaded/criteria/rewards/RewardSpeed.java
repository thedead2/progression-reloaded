package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import de.thedead2.progression_reloaded.player.data.AbilityStats.SpeedType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static de.thedead2.progression_reloaded.player.data.AbilityStats.SpeedType.*;

@ProgressionRule(name="speed", color=0xFFFFBF00, meta="speed")
public class RewardSpeed extends RewardBaseAbility {
    public float speed = 0.1F;
    public boolean land = true;
    public boolean air = false;
    public boolean water = true;

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.speed.description", speed, getType());
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.EDIT ? 100 : 55;
    }

    @Override
    public void addAbilityTooltip(List list) {
        list.add(TextFormatting.GRAY + getProvider().getLocalisedName() + ": " + speed + " " + getType());
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (player.worldObj.isRemote) {
                SpeedType type = player.isInWater() ? WATER : !player.onGround ? AIR : LAND;
                float speed = PlayerTracker.getClientPlayer().getAbilities().getSpeed(type);
                if (speed != 1F) {
                    player.motionX *= speed;
                    player.motionZ *= speed;
                }
            }
        }
    }

    @Override
    public void reward(EntityPlayerMP player) {
        if (land) PlayerTracker.getServerPlayer(player).addSpeed(LAND, speed);
        if (air) PlayerTracker.getServerPlayer(player).addSpeed(AIR, speed);
        if (water) PlayerTracker.getServerPlayer(player).addSpeed(WATER, speed);
    }


    //Helper methods
    public String[] translate(String... text) {
        String[] translated = new String[text.length];
        for (int i = 0; i < translated.length; i++) {
            translated[i] = de.thedead2.progression_reloaded.ProgressionReloaded.translate("reward.speed." + text[i]);
        }

        return translated;
    }

    private String getType() {
        if (land && water && air) return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.speed.format3", (Object[])translate("water.description", "land.description", "air.description"));
        else if (land && water) return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.speed.format2", (Object[])translate("water.description", "land.description"));
        else if (water && air) return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.speed.format2", (Object[])translate("water.description", "air.description"));
        else if (land && air) return de.thedead2.progression_reloaded.ProgressionReloaded.format("reward.speed.format2", (Object[])translate("land.description", "air.description"));
        else if (land) return de.thedead2.progression_reloaded.ProgressionReloaded.translate("reward.speed.land.description");
        else if (water) return de.thedead2.progression_reloaded.ProgressionReloaded.translate("reward.speed.water.description");
        else if (air) return de.thedead2.progression_reloaded.ProgressionReloaded.translate("reward.speed.air.description");
        else return de.thedead2.progression_reloaded.ProgressionReloaded.translate("reward.speed.never");
    }
}
