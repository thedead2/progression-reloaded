package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import javax.annotation.Nullable;


public class PlayerPredicate implements ITriggerPredicate<PlayerData> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("player");

    public static final PlayerPredicate ANY = new PlayerPredicate(MinMax.Ints.ANY, null, null, null);

    private final MinMax.Ints xp;

    @Nullable
    private final GameType gameMode;

    private final ProgressionLevel level;

    private final PlayerTeam team;


    public PlayerPredicate(MinMax.Ints xp, @Nullable GameType gameMode, ProgressionLevel level, PlayerTeam team) {
        this.xp = xp;
        this.gameMode = gameMode;
        this.level = level;
        this.team = team;
    }



    public static PlayerPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        MinMax.Ints experienceLevel = MinMax.Ints.fromJson(jsonObject.get("xp"));
        ProgressionLevel level = SerializationHelper.getNullable(jsonObject, "level", jsonElement1 -> ProgressionLevel.fromKey(ResourceLocation.tryParse(jsonElement1.getAsString())));
        PlayerTeam team = SerializationHelper.getNullable(jsonObject, "team", jsonElement1 -> PlayerTeam.fromKey(ResourceLocation.tryParse(jsonElement1.getAsString())));
        GameType gameMode = SerializationHelper.getNullable(jsonObject, "gameMode", jsonElement1 -> GameType.byName(jsonElement1.getAsString(), null));

        return new PlayerPredicate(experienceLevel, gameMode, level, team);
    }


    @Override
    public boolean matches(PlayerData player, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        ServerPlayer serverplayer = player.getServerPlayer();
        if(this.level != null && !player.hasProgressionLevel(this.level)) {
            return false;
        }
        else if(this.team != null && !player.isInTeam(this.team)) {
            return false;
        }
        else if(!this.xp.matches(serverplayer.experienceLevel)) {
            return false;
        }
        else {
            return this.gameMode == null || this.gameMode == serverplayer.gameMode.getGameModeForPlayer();
        }
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("xp", this.xp.toJson());
        SerializationHelper.addNullable(this.level, jsonObject, "level", level -> new JsonPrimitive(level.getId().toString()));
        SerializationHelper.addNullable(this.team, jsonObject, "team", team -> new JsonPrimitive(team.getId().toString()));
        SerializationHelper.addNullable(this.gameMode, jsonObject, "gameMode", gameType -> new JsonPrimitive(gameType.getName()));

        return jsonObject;
    }


    @Override
    public Component getDefaultDescription() {
        return Component.empty();
    }
}
