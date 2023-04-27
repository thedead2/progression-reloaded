package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.DeferredRegister;

public class TeamData extends SavedData {
    private static final DeferredRegister<PlayerTeam> TEAMS = DeferredRegister.create(ModRegistries.Keys.TEAM_DATA, ModHelper.MOD_ID);

    public TeamData(){
        this.setDirty();
    }
    @Override
    public CompoundTag save(CompoundTag tag) {
        ModRegistries.PROGRESSION_TEAM_DATA.get().getValues().forEach(playerTeam -> tag.put(playerTeam.getId().toString(), playerTeam.toCompoundTag()));
        return tag;
    }

    public static TeamData load(CompoundTag tag) {
        tag.getAllKeys().forEach(s -> TEAMS.register(s, () -> PlayerTeam.fromCompoundTag(tag.getCompound(s))));
        return new TeamData();
    }
}
