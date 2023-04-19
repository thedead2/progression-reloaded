package de.thedead2.progression_reloaded.handlers;

import de.thedead2.progression_reloaded.api.ICustomDataBuilder;
import de.thedead2.progression_reloaded.api.IProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.*;
import de.thedead2.progression_reloaded.api.special.IRequestItem;
import de.thedead2.progression_reloaded.crafting.ActionType;
import de.thedead2.progression_reloaded.criteria.Condition;
import de.thedead2.progression_reloaded.criteria.Filter;
import de.thedead2.progression_reloaded.criteria.Reward;
import de.thedead2.progression_reloaded.criteria.Trigger;
import de.thedead2.progression_reloaded.criteria.rewards.RewardHurt;
import de.thedead2.progression_reloaded.helpers.CraftingHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.network.PacketFireTrigger;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketRequestItem;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import java.util.HashMap;
import java.util.UUID;

public class APIHandler implements IProgressionAPI {
    //This is the registry for trigger type and reward type creation
    public static final HashMap<String, ITriggerProvider> triggerTypes = new HashMap();
    public static final HashMap<String, IRewardProvider> rewardTypes = new HashMap();
    public static final HashMap<String, IConditionProvider> conditionTypes = new HashMap();
    public static final HashMap<String, IFilterProvider> filterTypes = new HashMap();

    @Override
    //Fired Server Side only
    public Result fireTrigger(UUID uuid, String string, Object... data) {
        return PlayerTracker.getServerPlayer(uuid).getMappings().fireAllTriggers(string, data);
    }

    @Override
    //Fired Server Side only
    public Result fireTrigger(EntityPlayer player, String string, Object... data) {
        if (!player.worldObj.isRemote) {
            return fireTrigger(PlayerHelper.getUUIDForPlayer(player), string, data);
        } else return Result.DEFAULT;
    }

    @Override
    public void fireTriggerClientside(String trigger, Object... data) {
        PacketHandler.sendToServer(new PacketFireTrigger(trigger, data));
    }

    @Override
    public void forceComplete(ICriteria criteria) {
        fireTriggerClientside("complete", criteria);
    }

    @Override
    public void registerCustomDataBuilder(String trigger, ICustomDataBuilder builder) {
        PacketFireTrigger.handlers.put(trigger, builder);
    }

    public static IConditionProvider registerConditionType(IRule rule, String unlocalised) {
        try {
            String name = "condition." + unlocalised;
            IConditionProvider dummy = new Condition((ICondition)rule.getClass().newInstance(), name);
            conditionTypes.put(name, dummy);
            return dummy;
        } catch (Exception e) {return null; }
    }

    public static ITriggerProvider registerTriggerType(IRule rule, String unlocalised, int color) {
        try {
            String name = "trigger." + unlocalised;
            ITriggerProvider dummy = new Trigger((ITrigger)rule.getClass().newInstance(), name, color);
            triggerTypes.put(name, dummy);
            return dummy;
        } catch (Exception e) { return null; }
    }

    public static IRewardProvider registerRewardType(IRule rule, String unlocalised, int color) {
        try {
            String name = "reward." + unlocalised;
            IRewardProvider dummy = new Reward((IReward)rule.getClass().newInstance(), name, color);
            rewardTypes.put(name, dummy);
            return dummy;
        } catch (Exception e) { return null; }
    }


    public static IFilterProvider registerFilterType(IRule rule, String unlocalised, int color) {
        try {
            IFilter filter = (IFilter)rule.getClass().newInstance();
            String name = "filter." + filter.getType().getName() + "." + unlocalised;
            IFilterProvider dummy = new Filter(filter, name, color);
            filterTypes.put(name, dummy);
            return dummy;
        } catch (Exception e) { return null; }
    }

    @Override
    public void registerDamageSource(DamageSource source) {
        RewardHurt.sources.put(source.damageType, source);
    }

    @Override
    public IAction registerActionType(String name) {
        return new ActionType(name.toUpperCase()); //WOOT!
    }

    @Override
    public void requestItem(IRequestItem reward, EntityPlayer player) {
        PacketHandler.sendToClient(new PacketRequestItem(reward.getProvider().getUniqueID()), player);
    }

    @Override
    public boolean canUseToPerformAction(String actionType, ItemStack stack, Object tileOrPlayer) {
        ActionType type = ActionType.getCraftingActionFromName(actionType);
        return CraftingHelper.canPerformActionAbstract(type, tileOrPlayer, stack);
    }
}
