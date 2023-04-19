package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ITab;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.event.TabVisibleEvent;
import de.thedead2.progression_reloaded.api.special.IGetterCallback;
import de.thedead2.progression_reloaded.api.special.IHasEventBus;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.api.special.IStoreNBTData;
import de.thedead2.progression_reloaded.handlers.APICache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

@ProgressionRule(name="tab.show", color=0xFFCCCCCC, meta="showTab")
public class RewardShowTab extends RewardBaseSingular implements IInit, IGetterCallback, IStoreNBTData, IHasEventBus  {
    public boolean hideByDefault = true;
    public String displayName = "";
    private UUID tabID = UUID.randomUUID();
    private ITab tab;

    @Override
    public void init(boolean isClient) {
        try {
            for (ITab t : APICache.getCache(isClient).getTabSet()) {
                String display = t.getLocalisedName();
                if (t.getLocalisedName().equals(displayName)) {
                    tab = t;
                    tabID = t.getUniqueID();
                    break;
                }
            }
        } catch (Exception e) {}
    }


    @Override
    public String getDescription() {
        if (tab != null) {
            String end = hideByDefault ? "show" : "hide";
            return format(end, tab.getLocalisedName());
        } else return translate("invalid");
    }

    @Override
    public String getField(String fieldName) {
        return tab != null ? TextFormatting.GREEN + displayName : TextFormatting.RED + displayName;
    }

    @Override
    public EventBus getEventBus() {
        return MinecraftForge.EVENT_BUS;
    }

    @SubscribeEvent
    public void onFeatureRender(TabVisibleEvent event) {
        NBTTagCompound tag = ProgressionAPI.player.getCustomData(event.getEntityPlayer(), "progression.tab.hidden");
        if (tag != null) {
            if (tag.hasKey(event.unique.toString())) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public String getNBTKey() {
        return "progression.tab.hidden";
    }

    @Override
    public NBTTagCompound getDefaultTags(NBTTagCompound tag) {
        if (hideByDefault) {
            if (tab != null) {
                tag.setBoolean(tab.getUniqueID().toString(), true);
            }
        }

        return tag;
    }

    @Override
    public void reward(EntityPlayerMP player) {
        if (tab == null) return; //Do not give the reward

        NBTTagCompound tag = ProgressionAPI.player.getCustomData(player, "progression.tab.hidden");
        if (tag == null) tag = new NBTTagCompound();
        if (hideByDefault) tag.removeTag(tab.getUniqueID().toString());
        else tag.setBoolean(tab.getUniqueID().toString(), true);

        ProgressionAPI.player.setCustomData(player, "progression.tab.hidden", tag);
    }
}
