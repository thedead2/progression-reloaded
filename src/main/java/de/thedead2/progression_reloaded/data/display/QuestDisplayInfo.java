package de.thedead2.progression_reloaded.data.display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;


public record QuestDisplayInfo(ResourceLocation id, Component title, Component description, ItemStack icon, boolean mainQuest, @Nullable ResourceLocation parentQuest) implements IDisplayInfo<ProgressionQuest> {


    public static QuestDisplayInfo fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Component title = Component.Serializer.fromJson(jsonObject.get("title"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        ItemStack icon = JsonHelper.itemFromJson(jsonObject.get("icon").getAsJsonObject());
        boolean mainQuest = jsonObject.get("isMainQuest").getAsBoolean();
        ResourceLocation parent = SerializationHelper.getNullable(jsonObject, "parent", jsonElement1 -> new ResourceLocation(jsonElement1.getAsString()));

        return new QuestDisplayInfo(id, title, description, icon, mainQuest, parent);
    }


    public static QuestDisplayInfo fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component title = buf.readComponent();
        Component description = buf.readComponent();
        ItemStack icon = buf.readItem();
        boolean mainQuest = buf.readBoolean();
        ResourceLocation parentQuest = buf.readNullable(FriendlyByteBuf::readResourceLocation);

        return new QuestDisplayInfo(id, title, description, icon, mainQuest, parentQuest);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeComponent(this.title);
        buf.writeComponent(this.description);
        buf.writeItem(this.icon);
        buf.writeBoolean(this.mainQuest);
        buf.writeNullable(this.parentQuest, FriendlyByteBuf::writeResourceLocation);
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", this.id.toString());
        jsonObject.add("title", Component.Serializer.toJsonTree(this.title));
        jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
        jsonObject.add("icon", JsonHelper.itemToJson(this.icon));
        jsonObject.addProperty("isMainQuest", this.mainQuest);
        SerializationHelper.addNullable(this.parentQuest, jsonObject, "parent", id -> new JsonPrimitive(id.toString()));

        return jsonObject;
    }


    @Override
    public String toString() {
        return "QuestDisplayInfo[" +
                "id=" + id + ", " +
                "title=" + title.getString() + ", " +
                "description=" + description.getString() + ", " +
                "icon=" + icon + ", " +
                "mainQuest=" + mainQuest + ", " +
                "parentQuest=" + parentQuest + ']';
    }


    public Builder deconstruct() {
        Builder builder = Builder.builder();
        builder.id = this.id;
        builder.title = this.title;
        builder.description = this.description;
        builder.icon = this.icon;
        builder.isMainQuest = this.mainQuest;
        builder.parentQuest = this.parentQuest;

        return builder;
    }

    public static class Builder {

        private ResourceLocation id;

        private Component title;

        private Component description = Component.empty();

        private ItemStack icon = ItemStack.EMPTY;

        private boolean isMainQuest = false;

        @Nullable
        private ResourceLocation parentQuest;


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


        public Builder isMainQuest() {
            this.isMainQuest = true;

            return this;
        }


        public Builder withParent(String parent) {
            return this.withParent(new ResourceLocation(ModHelper.MOD_ID, parent));
        }


        public Builder withParent(ResourceLocation parent) {
            this.parentQuest = parent;

            return this;
        }


        public QuestDisplayInfo build() {
            if(!this.canBuild()) {
                throw new IllegalStateException("Can't build DisplayInfo for quest with missing id or title!");
            }

            return new QuestDisplayInfo(this.id, this.title, this.description, this.icon, this.isMainQuest, this.parentQuest);
        }


        private boolean canBuild() {
            return this.id != null && this.title != null;
        }
    }
}
