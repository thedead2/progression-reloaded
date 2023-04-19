package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ICustomTooltip;
import de.thedead2.progression_reloaded.lib.FakeOp;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;

@ProgressionRule(name="command", color=0xFF2626FF, icon="minecraft:command_block")
public class RewardCommand extends RewardBase implements ICustomDescription, ICustomTooltip {
    public String command = "dummy";

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", command);
    }

    @Override
    public void addTooltip(List list) {
        list.add(de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName() + ".execute"));
        list.add(TextFormatting.GRAY + command);
    }

    @Override
    public void reward(EntityPlayerMP player) {
        String newCommand = command.replace("@u", player.getDisplayNameString());
        FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(FakeOp.getInstance(), newCommand);
    }
}
