package de.thedead2.progression_reloaded.data.display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;


public final class LevelDisplayInfo implements IDisplayInfo<ProgressionLevel> {

    private final ResourceLocation id;

    private final Component title;

    private final Component description;

    private final ItemStack icon;

    @Nullable
    private final ResourceLocation previousLevel;


    public LevelDisplayInfo(ResourceLocation id, Component title, Component description, ItemStack icon, @Nullable ResourceLocation previousLevel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.previousLevel = previousLevel;
    }


    public static LevelDisplayInfo fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Component title = Component.Serializer.fromJson(jsonObject.get("title"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        ItemStack icon = JsonHelper.itemFromJson(jsonObject.get("icon").getAsJsonObject());

        ResourceLocation previous = null;
        if(jsonObject.has("previous")) {
            previous = new ResourceLocation(jsonObject.get("previous").getAsString());
        }

        return new LevelDisplayInfo(id, title, description, icon, previous);
    }


    public static LevelDisplayInfo fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Component description = buf.readComponent();
        ItemStack icon = buf.readItem();
        ResourceLocation previous = buf.readNullable(FriendlyByteBuf::readResourceLocation);

        return new LevelDisplayInfo(id, title, description, icon, previous);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeComponent(this.title);
        buf.writeComponent(this.description);
        buf.writeItem(this.icon);
        buf.writeNullable(this.previousLevel, FriendlyByteBuf::writeResourceLocation);
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.add("title", Component.Serializer.toJsonTree(this.title));
        jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
        jsonObject.add("icon", JsonHelper.itemToJson(this.icon));

        if(this.previousLevel != null) {
            jsonObject.addProperty("previous", this.previousLevel.toString());
        }

        return jsonObject;
    }


    @Override
    public ItemStack getIcon() {
        return icon;
    }


    public Component getTitle() {
        return title;
    }


    @Override
    public Component getDescription() {
        return description;
    }


    @Override
    public ResourceLocation getId() {
        return id;
    }


    @Nullable
    public ResourceLocation previousLevel() {return previousLevel;}


    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, icon, previousLevel);
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (LevelDisplayInfo) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.icon, that.icon) &&
                Objects.equals(this.previousLevel, that.previousLevel);
    }


    @Override
    public String toString() {
        return "LevelDisplayInfo[" +
                "uuid=" + id + ", " +
                "title=" + title + ", " +
                "description=" + description + ", " +
                "icon=" + icon + ", " +
                "previousLevel=" + previousLevel + ']';
    }


    public static class Builder {

        private ResourceLocation id;

        private Component title;

        private Component description = Component.empty();

        private ItemStack icon = ItemStack.EMPTY;

        @Nullable
        private ResourceLocation previousLevel;


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withId(String id) {
            return this.withId(new ResourceLocation(ModHelper.MOD_ID, id));
        }


        public Builder withId(ResourceLocation id) {
            this.id = id;

            return this;
        }


        public Builder withName(String title) {
            return this.withName(Component.literal(title));
        }


        public Builder withName(Component title) {
            this.title = title;

            return this;
        }


        public Builder withDescription(String description) {
            return this.withDescription(Component.literal(description));
        }


        public Builder withDescription(Component description) {
            this.description = description;

            return this;
        }


        public Builder withIcon(Item item) {
            return this.withIcon(new ItemStack(item));
        }


        public Builder withIcon(ItemStack item) {
            this.icon = item;

            return this;
        }


        public Builder withParent(String parent) {
            return this.withParent(new ResourceLocation(ModHelper.MOD_ID, parent));
        }


        public Builder withParent(ResourceLocation parent) {
            this.previousLevel = parent;

            return this;
        }


        public LevelDisplayInfo build() {
            if(!this.canBuild()) {
                throw new IllegalStateException("Can't build DisplayInfo for level with missing uuid or title!");
            }

            return new LevelDisplayInfo(this.id, this.title, this.description, this.icon, this.previousLevel);
        }


        private boolean canBuild() {
            return this.id != null;
        }
    }
}
