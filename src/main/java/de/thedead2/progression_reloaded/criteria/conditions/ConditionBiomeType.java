package de.thedead2.progression_reloaded.criteria.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ISetterCallback;
import de.thedead2.progression_reloaded.api.special.ISpecialJSON;
import de.thedead2.progression_reloaded.helpers.JSONHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

@ProgressionRule(name="biomeType", color=0xFF00B200, meta="ifIsBiome")
public class ConditionBiomeType extends ConditionBase implements ICustomDescription, ISetterCallback, ISpecialJSON {
    private Type[] theBiomeTypes = new Type[] { Type.FOREST };
    public String biomeTypes = "forest";

    @Override
    public String getDescription() {
        if (getProvider().isInverted()) return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description.inverted", biomeTypes);
        return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", biomeTypes);
    }

    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        for (EntityPlayer player: team.getTeamEntities()) { //If any team member has the achievement
            Type types[] = BiomeDictionary.getTypesForBiome(player.worldObj.getBiome(new BlockPos(player)));
            for (Type type : theBiomeTypes) {
                for (Type compare : types) {
                    if (compare == type) return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onlySpecial() {
        return true;
    }

    @Override
    public boolean setField(String fieldName, Object object) {
        String fieldValue = (String) object;
        String[] split = fieldValue.split(",");
        StringBuilder fullString = new StringBuilder();
        try {
            Type[] types = new Type[split.length];
            for (int i = 0; i < types.length; i++) {
                types[i] = Type.getType(split[i].trim());
            }

            theBiomeTypes = types;
        } catch (Exception e) {}

        biomeTypes = fieldValue;
        return true;
    }

    @Override
    public void readFromJSON(JsonObject data) {
        ConditionBiomeType condition = new ConditionBiomeType();
        JsonArray array = data.get("types").getAsJsonArray();
        Type[] types = new Type[array.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = Type.getType(array.get(i).getAsString());
        }

        theBiomeTypes = types;
        biomeTypes = JSONHelper.getString(data, "string", "forest");
    }

    @Override
    public void writeToJSON(JsonObject data) {
        JsonArray array = new JsonArray();
        for (Type t : theBiomeTypes) {
            array.add(new JsonPrimitive(t.name().toLowerCase()));
        }

        data.add("types", array);
        JSONHelper.setString(data, "string", biomeTypes, "forest");
    }
}
