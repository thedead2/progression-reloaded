package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;


public class PlayerPredicate implements ITriggerPredicate<SinglePlayer> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("player");

    public static final PlayerPredicate ANY = new PlayerPredicate(MinMax.Ints.ANY, null, null, null, EntityPredicate.ANY);

    public static final int LOOKING_AT_RANGE = 100;

    private final MinMax.Ints experienceLevel;

    @Nullable
    private final GameType gameMode;

    private final ProgressionLevel level;

    private final PlayerTeam team;

    private final EntityPredicate lookingAtEntity;


    public PlayerPredicate(MinMax.Ints experienceLevel, @Nullable GameType gameMode, ProgressionLevel level, PlayerTeam team, EntityPredicate lookingAtEntity) {
        this.experienceLevel = experienceLevel;
        this.gameMode = gameMode;
        this.level = level;
        this.team = team;
        this.lookingAtEntity = lookingAtEntity;
    }


    public static PlayerPredicate from(SinglePlayer player) {
        ServerPlayer serverPlayer = player.getServerPlayer();
        int experienceLevel = serverPlayer.experienceLevel;
        GameType gameMode = serverPlayer.gameMode.getGameModeForPlayer();

        Entity entity = null;
        Vec3 vec3 = serverPlayer.getEyePosition();
        Vec3 vec31 = serverPlayer.getViewVector(1.0F);
        Vec3 vec32 = vec3.add(vec31.x * LOOKING_AT_RANGE, vec31.y * LOOKING_AT_RANGE, vec31.z * LOOKING_AT_RANGE);
        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
                serverPlayer.level,
                serverPlayer,
                vec3,
                vec32,
                (new AABB(vec3, vec32)).inflate(1.0D),
                (entity1) -> !entity1.isSpectator(),
                0.0F
        );
        if(entityhitresult == null || entityhitresult.getType() != HitResult.Type.ENTITY) {
            entity = entityhitresult.getEntity();
        }

        return new PlayerPredicate(
                MinMax.Ints.exactly(experienceLevel),
                gameMode,
                player.getProgressionLevel(),
                player.getTeam().orElse(null),
                entity == null ? EntityPredicate.ANY : EntityPredicate.from(entity)
        );
    }


    public static PlayerPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        MinMax.Ints experienceLevel = MinMax.Ints.fromJson(jsonObject.get("experience_level"));
        ProgressionLevel level = ProgressionLevel.fromKey(ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "level", null)));
        PlayerTeam team1 = PlayerTeam.fromKey(ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "team", null)));
        GameType gametype = GameType.byName(GsonHelper.getAsString(jsonObject, "gamemode", ""), null);

        EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonObject.get("looking_at"));

        return new PlayerPredicate(experienceLevel, gametype, level, team1, entitypredicate);
    }


    @Override
    public boolean matches(SinglePlayer player, Object... addArgs) {
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
        else if(!this.experienceLevel.matches(serverplayer.experienceLevel)) {
            return false;
        }
        else if(this.gameMode != null && this.gameMode != serverplayer.gameMode.getGameModeForPlayer()) {
            return false;
        }
        else if(this.lookingAtEntity != EntityPredicate.ANY) {
            Vec3 vec3 = serverplayer.getEyePosition();
            Vec3 vec31 = serverplayer.getViewVector(1.0F);
            Vec3 vec32 = vec3.add(vec31.x * LOOKING_AT_RANGE, vec31.y * LOOKING_AT_RANGE, vec31.z * LOOKING_AT_RANGE);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
                    serverplayer.level,
                    serverplayer,
                    vec3,
                    vec32,
                    (new AABB(vec3, vec32)).inflate(1.0D),
                    (entity) -> !entity.isSpectator(),
                    0.0F
            );

            if(entityhitresult == null || entityhitresult.getType() != HitResult.Type.ENTITY) {
                return false;
            }

            Entity entity = entityhitresult.getEntity();
            return this.lookingAtEntity.matches(serverplayer, entity) && serverplayer.hasLineOfSight(entity);
        }

        return true;
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("experience_level", this.experienceLevel.serializeToJson());
        if(this.level != null) {
            jsonObject.addProperty("level", this.level.getId().toString());
        }
        if(this.team != null) {
            jsonObject.addProperty("team", this.team.getId().toString());
        }
        if(this.gameMode != null) {
            jsonObject.addProperty("gamemode", this.gameMode.getName());
        }

        jsonObject.add("looking_at", this.lookingAtEntity.toJson());
        return jsonObject;
    }
}
