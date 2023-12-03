package de.thedead2.progression_reloaded.data.quest;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.criteria.QuestCriteria;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;


public class QuestActions {

    private final Map<ResourceLocation, ActionNode> actions = Maps.newHashMap();

    private final Map<NodeType, Set<ActionNode>> nodesByType = Maps.newHashMap();


    private QuestActions(Map<ResourceLocation, ActionNode> actions) {
        this.actions.putAll(actions);

        for(var node : this.actions.values()) {
            NodeType type = node.getType();
            this.nodesByType.compute(type, (nodeType, actionNodes) -> (actionNodes == null ? new HashSet<>() : actionNodes)).add(node);
        }
    }


    public static QuestActions loadFromNBT(CompoundTag tag) {
        Map<ResourceLocation, ActionNode> actions = CollectionHelper.loadFromNBT(Maps::newHashMapWithExpectedSize, tag, ResourceLocation::tryParse, tag1 -> ActionNode.loadFromNBT((CompoundTag) tag1));

        return new QuestActions(actions);
    }


    public static QuestActions fromNetwork(FriendlyByteBuf buf) {
        Map<ResourceLocation, ActionNode> actions = buf.readMap(Maps::newHashMapWithExpectedSize, FriendlyByteBuf::readResourceLocation, ActionNode::fromNetwork);

        return new QuestActions(actions);
    }


    public static QuestActions fromJson(JsonObject jsonObject) {
        Map<ResourceLocation, ActionNode> actions = CollectionHelper.loadFromJson(Maps::newHashMapWithExpectedSize, jsonObject, ResourceLocation::tryParse, jsonElement -> ActionNode.fromJson(jsonElement.getAsJsonObject()));

        return new QuestActions(actions);
    }


    @NotNull
    public QuestActions.ActionNode getNodeForId(ResourceLocation id) {
        return getNodeForId(this.actions, id);
    }


    @NotNull
    public static QuestActions.ActionNode getNodeForId(Map<ResourceLocation, ActionNode> nodes, ResourceLocation id) {
        ActionNode actionNode = nodes.get(id);
        if(actionNode == null) {
            throw new IllegalArgumentException("Unknown node for id: " + id);
        }
        return actionNode;
    }


    public ResourceLocation getIdForNode(ActionNode actionNode) {
        return actionNode.id;
    }


    public ImmutableSet<ActionNode> getNodesByType(NodeType type) {
        return ImmutableSet.copyOf(this.nodesByType.get(type));
    }


    public @NotNull CompoundTag saveToNBT() {
        return CollectionHelper.saveToNBT(this.actions, Object::toString, ActionNode::saveToNBT);
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeMap(this.actions, FriendlyByteBuf::writeResourceLocation, (buf1, actionNode) -> actionNode.toNetwork(buf1));
    }


    public JsonElement toJson() {
        return CollectionHelper.saveToJson(this.actions, Object::toString, ActionNode::toJson);
    }


    public ResourceLocation getLastNodeId(boolean successful) {
        for(var entry : this.actions.entrySet()) {
            ActionNode actionNode = entry.getValue();
            if(actionNode.getType() == NodeType.END_NODE && actionNode.getQuestStatus() == (successful ? ProgressionQuest.Status.COMPLETE : ProgressionQuest.Status.FAILED)) {
                return entry.getKey();
            }
        }

        return null;
    }


    public void forEach(BiConsumer<ResourceLocation, ActionNode> consumer) {
        this.actions.forEach(consumer);
    }


    public enum NodeType {
        START_NODE,
        POSSIBLE_START_NODE,
        DEFAULT,
        END_NODE
    }

    public static class Builder {

        private final Map<ResourceLocation, ActionNode> actions = new HashMap<>();


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withStart(String id, QuestCriteria criteria, Component description, String... children) {
            return this.addNode(ActionNode.newStartNode(ActionNode.createId(id), criteria, description, CollectionHelper.convertCollection(Sets.newHashSet(children), Sets::newHashSetWithExpectedSize, ActionNode::createId)));
        }


        private Builder addNode(ActionNode node) {
            this.actions.put(node.id, node);

            return this;
        }


        public Builder withEnd(String id, QuestCriteria criteria, Component description, boolean successful) {
            return this.addNode(ActionNode.newEndNode(ActionNode.createId(id), criteria, description, Sets.newHashSet(), successful));
        }


        public Builder withAction(String id, QuestCriteria criteria, Component description, boolean optional, boolean possibleStartNode, String... children) {
            return this.addNode(ActionNode.newActionNode(ActionNode.createId(id), criteria, description, Sets.newHashSet(), CollectionHelper.convertCollection(Sets.newHashSet(children), Sets::newHashSetWithExpectedSize, ActionNode::createId), optional, possibleStartNode));
        }


        public QuestActions build() {
            this.checkNodes();
            return new QuestActions(this.actions);
        }


        private void checkNodes() {
            boolean startNode = false, endNode = false;
            for(ActionNode node : this.actions.values()) {
                NodeType type = node.getType();
                if(type == NodeType.START_NODE) {
                    startNode = true;
                }
                else if(type == NodeType.END_NODE) {
                    endNode = true;
                }

                node.getChildren().forEach(id -> {
                    ActionNode child = this.actions.get(id);
                    child.addParent(node.id);
                });
            }

            if(!startNode) {
                throw new IllegalStateException("Missing start node! Each quest needs to have at least one start node!");
            }
            if(!endNode) {
                throw new IllegalStateException("Missing end node! Each quest needs to have at least one end node!");
            }
        }
    }

    public static class ActionNode {

        private final ResourceLocation id;

        private final NodeType nodeType;

        private final ProgressionQuest.Status questStatus;

        private final QuestCriteria criteria;

        private final Component description;

        private final Set<ResourceLocation> parents = Sets.newHashSet();

        private final Set<ResourceLocation> children = Sets.newHashSet();

        private final boolean optional;


        private ActionNode(ResourceLocation id, NodeType nodeType, ProgressionQuest.Status questStatus, QuestCriteria criteria, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, boolean optional) {
            this.id = id;
            this.nodeType = nodeType;
            this.questStatus = questStatus;
            this.criteria = criteria;
            this.description = description;
            this.parents.addAll(parents);
            this.children.addAll(children);
            this.optional = optional;
        }


        public static ActionNode newStartNode(ResourceLocation id, QuestCriteria criteria, Component description, Set<ResourceLocation> children) {
            return new ActionNode(id, NodeType.START_NODE, ProgressionQuest.Status.NOT_STARTED, criteria, description, Sets.newHashSet(), children, false);
        }


        public static ActionNode newEndNode(ResourceLocation id, QuestCriteria criteria, Component description, Set<ResourceLocation> parents, boolean successful) {
            return new ActionNode(id, NodeType.END_NODE, successful ? ProgressionQuest.Status.COMPLETE : ProgressionQuest.Status.FAILED, criteria, description, parents, Sets.newHashSet(), false);
        }


        public static ActionNode newActionNode(ResourceLocation id, QuestCriteria criteria, Component description, Set<ResourceLocation> parents, Set<ResourceLocation> children, boolean optional, boolean possibleStartNode) {
            return new ActionNode(id, possibleStartNode ? NodeType.POSSIBLE_START_NODE : NodeType.DEFAULT, ProgressionQuest.Status.ACTIVE, criteria, description, parents, children, optional);
        }


        public static ResourceLocation createId(String id) {
            return new ResourceLocation(ModHelper.MOD_ID, id + "_node");
        }


        public static ActionNode fromNetwork(FriendlyByteBuf buf) {
            ResourceLocation id = buf.readResourceLocation();
            NodeType nodeType = buf.readEnum(NodeType.class);
            ProgressionQuest.Status questStatus = buf.readEnum(ProgressionQuest.Status.class);
            QuestCriteria criteria = QuestCriteria.fromNetwork(buf);
            Component description = buf.readComponent();
            Set<ResourceLocation> children = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
            Set<ResourceLocation> parents = buf.readNullable(buf1 -> buf1.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation));
            boolean optional = buf.readBoolean();

            return new ActionNode(id, nodeType, questStatus, criteria, description, parents, children, optional);
        }


        public static ActionNode fromJson(JsonObject jsonObject) {
            ResourceLocation id = new ResourceLocation(jsonObject.get("id").getAsString());
            NodeType nodeType = NodeType.valueOf(jsonObject.get("type").getAsString());
            ProgressionQuest.Status questStatus = ProgressionQuest.Status.valueOf(jsonObject.get("status").getAsString());
            QuestCriteria criteria = QuestCriteria.fromJson(jsonObject.get("criteria"));
            Component description = Component.Serializer.fromJson(jsonObject.get("description"));
            Set<ResourceLocation> children = SerializationHelper.getNullable(jsonObject, "children", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
            Set<ResourceLocation> parents = SerializationHelper.getNullable(jsonObject, "parents", jsonElement -> CollectionHelper.loadFromJson(HashSet::new, jsonElement.getAsJsonArray(), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString())));
            boolean optional = jsonObject.get("optional").getAsBoolean();

            return new ActionNode(id, nodeType, questStatus, criteria, description, parents, children, optional);
        }


        public static ActionNode loadFromNBT(CompoundTag tag) {
            ResourceLocation id = new ResourceLocation(tag.getString("id"));
            NodeType nodeType = NodeType.valueOf(tag.getString("type"));
            ProgressionQuest.Status questStatus = ProgressionQuest.Status.valueOf(tag.getString("status"));
            QuestCriteria criteria = QuestCriteria.loadFromNBT(tag.getCompound("criteria"));
            Component description = Component.Serializer.fromJson(tag.getString("description"));
            Set<ResourceLocation> children = SerializationHelper.getNullable(tag, "children", tag1 -> CollectionHelper.loadFromNBT(HashSet::new, (ListTag) tag1, tag2 -> ResourceLocation.tryParse(tag2.getAsString())));
            Set<ResourceLocation> parents = SerializationHelper.getNullable(tag, "parents", tag1 -> CollectionHelper.loadFromNBT(HashSet::new, (ListTag) tag1, tag2 -> ResourceLocation.tryParse(tag2.getAsString())));
            boolean optional = tag.getBoolean("optional");

            return new ActionNode(id, nodeType, questStatus, criteria, description, parents, children, optional);
        }


        public void addChild(ActionNode actionNode) {
            if(this.nodeType == NodeType.END_NODE) {
                throw new IllegalStateException("A end action can't have any children!");
            }


            this.children.add(actionNode.id);
            actionNode.addParent(this);
        }


        public void addParent(ActionNode actionNode) {
            if(this.nodeType == NodeType.START_NODE) {
                throw new IllegalStateException("A starting action can't have any parents!");
            }

            this.parents.add(actionNode.id);
        }


        public void addChild(ResourceLocation nodeId) {
            if(this.nodeType == NodeType.END_NODE) {
                throw new IllegalStateException("An end action can't have any children!");
            }

            this.children.add(nodeId);
        }


        public void addParent(ResourceLocation nodeId) {
            if(this.nodeType == NodeType.START_NODE) {
                throw new IllegalStateException("A starting action can't have any parents!");
            }

            this.parents.add(nodeId);
        }


        public boolean hasChildren() {
            return !this.children.isEmpty();
        }


        public Component getDescription() {
            return description;
        }


        public ImmutableSet<ResourceLocation> getParents() {
            return ImmutableSet.copyOf(this.parents);
        }


        public Map<String, SimpleTrigger<?>> getCriteria() {
            return this.criteria.getCriteria();
        }


        public CriteriaStrategy getCriteriaStrategy() {
            return this.criteria.getCriteriaStrategy();
        }


        public CompoundTag saveToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", this.id.toString());
            tag.putString("type", this.nodeType.name());
            tag.putString("status", this.questStatus.name());
            tag.put("criteria", this.criteria.saveToNBT());
            tag.putString("description", Component.Serializer.toJsonTree(this.description).toString());
            SerializationHelper.addNullable(this.children, tag, "children", children -> CollectionHelper.saveToNBT(children, id -> StringTag.valueOf(id.toString())));
            SerializationHelper.addNullable(this.parents, tag, "parents", parents -> CollectionHelper.saveToNBT(parents, id -> StringTag.valueOf(id.toString())));
            tag.putBoolean("optional", this.optional);

            return tag;
        }


        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeResourceLocation(this.id);
            buf.writeEnum(this.nodeType);
            buf.writeEnum(this.questStatus);
            this.criteria.toNetwork(buf);
            buf.writeComponent(this.description);
            buf.writeNullable(this.children, (buf1, children) -> buf1.writeCollection(children, (FriendlyByteBuf::writeResourceLocation)));
            buf.writeNullable(this.parents, (buf1, parents) -> buf1.writeCollection(parents, (FriendlyByteBuf::writeResourceLocation)));
            buf.writeBoolean(this.optional);
        }


        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", this.id.toString());
            jsonObject.addProperty("type", this.nodeType.name());
            jsonObject.addProperty("status", this.questStatus.name());
            jsonObject.add("criteria", this.criteria.toJson());
            jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
            SerializationHelper.addNullable(this.children, jsonObject, "children", children -> CollectionHelper.saveToJson(children, id -> new JsonPrimitive(id.toString())));
            SerializationHelper.addNullable(this.parents, jsonObject, "parents", parents -> CollectionHelper.saveToJson(parents, id -> new JsonPrimitive(id.toString())));
            jsonObject.addProperty("optional", this.optional);

            return jsonObject;
        }


        public ImmutableSet<ResourceLocation> getChildren() {
            return ImmutableSet.copyOf(this.children);
        }


        public boolean isOptional() {
            return this.optional;
        }


        public NodeType getType() {
            return this.nodeType;
        }


        public boolean hasParents() {
            return this.parents.isEmpty();
        }


        public ProgressionQuest.Status getQuestStatus() {
            return this.questStatus;
        }


        public ResourceLocation getId() {
            return this.id;
        }
    }

    public static class NodeProgress implements Comparable<NodeProgress> {

        private final Map<String, CriterionProgress> criteria;

        private final CriteriaStrategy criteriaStrategy;


        public NodeProgress(ActionNode actionNode) {
            this(Maps.newHashMap(), actionNode.getCriteriaStrategy());
        }


        private NodeProgress(Map<String, CriterionProgress> criteria, CriteriaStrategy strategy) {
            this.criteria = criteria;
            this.criteriaStrategy = strategy;
        }


        public static NodeProgress loadFromNBT(CompoundTag tag) {
            Map<String, CriterionProgress> criteria = CollectionHelper.loadFromNBT(tag.getCompound("criteria"), s -> s, tag1 -> CriterionProgress.loadFromNBT((CompoundTag) tag1));
            CriteriaStrategy strategy = CriteriaStrategy.valueOf(tag.getString("strategy"));

            return new NodeProgress(criteria, strategy);
        }


        public static NodeProgress fromNetwork(FriendlyByteBuf buf) {
            Map<String, CriterionProgress> map = buf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
            CriteriaStrategy strategy = buf.readEnum(CriteriaStrategy.class);
            return new NodeProgress(map, strategy);
        }


        public static NodeProgress fromJson(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Map<String, CriterionProgress> criteria = CollectionHelper.loadFromJson(jsonObject.getAsJsonObject("criteria"), s -> s, CriterionProgress::fromJson);
            CriteriaStrategy strategy = CriteriaStrategy.valueOf(jsonObject.get("strategy").getAsString());

            return new NodeProgress(criteria, strategy);
        }


        public boolean isDone() {
            return this.criteriaStrategy.isDone(this);
        }


        public boolean hasProgress() {
            for(CriterionProgress criterionprogress : this.criteria.values()) {
                if(criterionprogress.isDone()) {
                    return true;
                }
            }

            return false;
        }


        public void updateProgress(ActionNode actionNode) {
            Set<String> set = actionNode.getCriteria().keySet();
            this.criteria.entrySet().removeIf((entry) -> !set.contains(entry.getKey()));

            for(String s : set) {
                if(!this.criteria.containsKey(s)) {
                    this.criteria.put(s, new CriterionProgress());
                }
            }
        }


        public float getPercent() {
            if(this.criteria.isEmpty()) {
                return 0.0F;
            }
            else {
                float f = (float) this.criteria.size();
                float f1 = (float) this.countCompletedCriteria();
                return f1 / f;
            }
        }


        private int countCompletedCriteria() {
            int i = 0;

            for(String s : criteria.keySet()) {
                CriterionProgress criterionprogress = this.getCriterion(s);
                if(criterionprogress != null && criterionprogress.isDone()) {
                    i++;
                }
            }

            return i;
        }


        @Nullable
        public CriterionProgress getCriterion(String criterionName) {
            return this.criteria.get(criterionName);
        }


        public Iterable<String> getRemainingCriteria() {
            List<String> list = Lists.newArrayList();

            for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
                if(!entry.getValue().isDone()) {
                    list.add(entry.getKey());
                }
            }

            return list;
        }


        public Iterable<String> getCompletedCriteria() {
            List<String> list = Lists.newArrayList();

            for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
                if(entry.getValue().isDone()) {
                    list.add(entry.getKey());
                }
            }

            return list;
        }


        public int compareTo(NodeProgress nodeProgress) {
            Date date = this.getFirstProgressDate();
            Date date1 = nodeProgress.getFirstProgressDate();
            if(date == null && date1 != null) {
                return 1;
            }
            else if(date != null && date1 == null) {
                return -1;
            }
            else {
                return date == null ? 0 : date.compareTo(date1);
            }
        }


        @Nullable
        public Date getFirstProgressDate() {
            Date date = null;

            for(CriterionProgress criterionprogress : this.criteria.values()) {
                if(criterionprogress.isDone() && (date == null || criterionprogress.getObtained().before(date))) {
                    date = criterionprogress.getObtained();
                }
            }

            return date;
        }


        public CompoundTag saveToNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("criteria", CollectionHelper.saveToNBT(this.criteria, s -> s, CriterionProgress::saveToCompoundTag));
            tag.putString("strategy", this.criteriaStrategy.name());
            return tag;
        }


        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf1, criterionProgress) -> criterionProgress.toNetwork(buf1));
            buf.writeEnum(this.criteriaStrategy);
        }


        public void reset() {
            this.criteria.forEach((s, criterionProgress) -> this.revokeProgress(s));
        }


        /**
         * Revokes the given criterion for this progress
         **/
        public boolean revokeProgress(String criterionName) {
            CriterionProgress criterionprogress = this.criteria.get(criterionName);
            if(criterionprogress != null && criterionprogress.isDone()) {
                criterionprogress.revoke();
                return true;
            }
            else {
                return false;
            }
        }


        public Map<String, CriterionProgress> getCriteria() {
            return criteria;
        }


        public void complete() {
            this.criteria.forEach((s, criterionProgress) -> this.grantProgress(s));
        }


        /**
         * Grants the given criterion to this progress
         **/
        public boolean grantProgress(String criterionName) {
            CriterionProgress criterionprogress = this.criteria.get(criterionName);
            if(criterionprogress != null && !criterionprogress.isDone()) {
                criterionprogress.grant();
                return true;
            }
            else {
                return false;
            }
        }


        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();

            jsonObject.add("criteria", CollectionHelper.saveToJson(this.criteria, s -> s, CriterionProgress::serializeToJson));
            jsonObject.addProperty("strategy", this.criteriaStrategy.name());

            return jsonObject;
        }
    }
}
