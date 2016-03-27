package joshie.progression.plugins.enchiridion.rewards;

import joshie.enchiridion.api.EnchiridionAPI;
import joshie.progression.criteria.rewards.RewardBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class RewardOpenBook extends RewardBase {
    public String bookid = "";
    public int page = 1;

    public RewardOpenBook() {
        super("open.book", 0xFFCCCCCC);
    }

    @Override
    public void reward(EntityPlayerMP player) {
        EnchiridionAPI.instance.openBook(player, bookid, page);
    }
}
