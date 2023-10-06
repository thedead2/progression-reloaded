package de.thedead2.progression_reloaded.data.display;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;


public final class QuestDisplayInfo implements IDisplayInfo {

    private final ResourceLocation id;

    private final Component title;

    private final Component description;

    private final ItemStack icon;

    private final boolean mainQuest;

    @Nullable
    private final ResourceLocation parentQuest;


    public QuestDisplayInfo(ResourceLocation id, Component title, Component description, ItemStack icon, boolean mainQuest, @Nullable ResourceLocation parentQuest) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.mainQuest = mainQuest;
        this.parentQuest = parentQuest;
    }


    public static QuestDisplayInfo fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
        Component title = Component.Serializer.fromJson(jsonObject.get("title"));
        Component description = Component.Serializer.fromJson(jsonObject.get("description"));
        ItemStack icon = JsonHelper.itemFromJson(jsonObject.get("icon").getAsJsonObject());
        boolean mainQuest = jsonObject.get("isMainQuest").getAsBoolean();
        ResourceLocation parent = jsonObject.has("parent") ? new ResourceLocation(jsonObject.get("parent").getAsString()) : null;

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
        if(this.parentQuest != null) {
            jsonObject.addProperty("parent", this.parentQuest.toString());
        }

        return jsonObject;
    }


    @Override
    public ItemStack getIcon() {
        return icon;
    }


    @Override
    public Component getTitle() {return title;}


    @Override
    public Component getDescription() {
        return description;
    }


    public ResourceLocation id() {return id;}


    public boolean mainQuest() {
        return mainQuest;
    }


    @Nullable
    public ResourceLocation parentQuest() {return parentQuest;}


    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, icon, mainQuest, parentQuest);
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (QuestDisplayInfo) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.icon, that.icon) &&
                this.mainQuest == that.mainQuest &&
                Objects.equals(this.parentQuest, that.parentQuest);
    }


    @Override
    public String toString() {
        return "QuestDisplayInfo[" +
                "id=" + id + ", " +
                "title=" + title + ", " +
                "description=" + description + ", " +
                "icon=" + icon + ", " +
                "mainQuest=" + mainQuest + ", " +
                "parentQuest=" + parentQuest + ']';
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
