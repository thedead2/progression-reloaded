package de.thedead2.progression_reloaded.client.display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;


public class LevelDisplayInfo {

    private final ResourceLocation id;

    private final Component title;

    private final Component description;

    private final ItemStack icon;

    @Nullable
    private final ResourceLocation previousLevel;

    @Nullable
    private final ResourceLocation nextLevel;


    public LevelDisplayInfo(ResourceLocation id, Component title, Component description, ItemStack icon, @Nullable ResourceLocation previousLevel, @Nullable ResourceLocation nextLevel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.previousLevel = previousLevel;
        this.nextLevel = nextLevel;
    }


    public static LevelDisplayInfo fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Component title = Component.Serializer.fromJson(jsonObject.get("title"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        ItemStack icon = JsonHelper.itemFromJson(jsonObject.get("icon").getAsJsonObject());

        ResourceLocation previous = null, next = null;
        if(jsonObject.has("previous")) {
            previous = new ResourceLocation(jsonObject.get("previous").getAsString());
        }
        if(jsonObject.has("next")) {
            next = new ResourceLocation(jsonObject.get("next").getAsString());
        }

        return new LevelDisplayInfo(id, title, description, icon, previous, next);
    }


    public static LevelDisplayInfo fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Component description = buf.readComponent();
        ItemStack icon = buf.readItem();
        ResourceLocation previous = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        ResourceLocation next = buf.readNullable(FriendlyByteBuf::readResourceLocation);

        return new LevelDisplayInfo(id, title, description, icon, previous, next);
    }


    public ResourceLocation getId() {
        return id;
    }


    public Component getTitle() {
        return title;
    }


    public Component getDescription() {
        return description;
    }


    public ItemStack getIcon() {
        return icon;
    }


    @Nullable
    public ResourceLocation getPreviousLevel() {
        return previousLevel;
    }


    @Nullable
    public ResourceLocation getNextLevel() {
        return nextLevel;
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.add("title", Component.Serializer.toJsonTree(this.title));
        jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
        jsonObject.add("icon", JsonHelper.itemToJson(this.icon));

        if(this.previousLevel != null) {
            jsonObject.addProperty("previous", this.previousLevel.toString());
        }
        if(this.nextLevel != null) {
            jsonObject.addProperty("next", this.nextLevel.toString());
        }

        return jsonObject;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeComponent(this.title);
        buf.writeComponent(this.description);
        buf.writeItem(this.icon);
        buf.writeNullable(this.previousLevel, FriendlyByteBuf::writeResourceLocation);
        buf.writeNullable(this.nextLevel, FriendlyByteBuf::writeResourceLocation);
    }


    public static class Builder {

        private ResourceLocation id;

        private Component title;

        private Component description = Component.empty();

        private ItemStack icon = ItemStack.EMPTY;

        @Nullable
        private ResourceLocation previousLevel;

        @Nullable
        private ResourceLocation nextLevel;


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


        public Builder withChild(String child) {
            return this.withChild(new ResourceLocation(ModHelper.MOD_ID, child));
        }


        public Builder withChild(ResourceLocation child) {
            this.nextLevel = child;

            return this;
        }


        public LevelDisplayInfo build() {
            if(!this.canBuild()) {
                throw new IllegalStateException("Can't build DisplayInfo for level with missing id or title!");
            }

            return new LevelDisplayInfo(this.id, this.title, this.description, this.icon, this.previousLevel, this.nextLevel);
        }


        private boolean canBuild() {
            return this.id != null && this.title != null;
        }
    }
}
