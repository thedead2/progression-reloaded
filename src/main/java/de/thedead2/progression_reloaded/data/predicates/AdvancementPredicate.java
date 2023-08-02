package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class AdvancementPredicate implements ITriggerPredicate<Advancement> {
    public static final ResourceLocation ID = ITriggerPredicate.createId("advancement");
    public static final AdvancementPredicate ANY = new AdvancementPredicate(null, null);
    private final Advancement advancement;
    private final AdvancementProgress advancementProgress;

    public AdvancementPredicate(Advancement advancement, AdvancementProgress advancementProgress) {
        this.advancement = advancement;
        this.advancementProgress = advancementProgress;
    }

    public static AdvancementPredicate fromJson(JsonElement advancement) {
        return null;
    }

    @Override
    public boolean matches(Advancement advancement, Object... addArgs) {
        if(this == ANY) return true;

        AdvancementProgress progress = (AdvancementProgress) addArgs[0];
        boolean flag1 = this.advancement.equals(advancement);
        boolean flag2 = this.advancementProgress == null || this.advancementProgress.compareTo(progress) == 0;
        return flag1 && flag2;
    }

    @Override
    public Map<String, Object> getFields() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public Builder<AdvancementPredicate> deconstruct() {
        return null;
    }

    @Override
    public ITriggerPredicate<Advancement> copy() {
        return null;
    }

    public static AdvancementPredicate from(Advancement advancement) {
        return new AdvancementPredicate(advancement, null);
    }
}
