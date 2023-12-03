package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Progress of a quest is dependent on the player or the team. Different players or teams can have different progress of a quest.
 **/

public class QuestProgress implements IProgressInfo<ProgressionQuest> {
    private final ProgressionQuest quest;

    private final Map<QuestActions.ActionNode, QuestActions.NodeProgress> nodeProgress;

    private ResourceLocation currentNode;


    public QuestProgress(ProgressionQuest quest) {
        this(null, quest, Maps.newHashMap());
    }


    private QuestProgress(ResourceLocation currentNode, ProgressionQuest quest, Map<QuestActions.ActionNode, QuestActions.NodeProgress> nodeProgress) {
        this.currentNode = currentNode;
        this.quest = quest;
        this.nodeProgress = nodeProgress;
    }


    public static QuestProgress fromNBT(CompoundTag tag) {
        ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag.getString("quest")));
        ResourceLocation currentPos = ResourceLocation.tryParse(tag.getString("currentPos"));
        Map<QuestActions.ActionNode, QuestActions.NodeProgress> nodeProgress = CollectionHelper.loadFromNBT(tag.getCompound("nodeProgress"), s -> quest.getActions().getNodeForId(ResourceLocation.tryParse(s)), tag1 -> QuestActions.NodeProgress.loadFromNBT((CompoundTag) tag1));

        return new QuestProgress(currentPos, quest, nodeProgress);
    }


    public static QuestProgress fromNetwork(FriendlyByteBuf buf) {
        ProgressionQuest quest = ModRegistries.QUESTS.get().getValue(buf.readResourceLocation());
        ResourceLocation currentPos = buf.readResourceLocation();
        Map<QuestActions.ActionNode, QuestActions.NodeProgress> nodeProgress = buf.readMap(buf1 -> quest.getActions().getNodeForId(buf1.readResourceLocation()), QuestActions.NodeProgress::fromNetwork);

        return new QuestProgress(currentPos, quest, nodeProgress);
    }


    public void updateProgress(PlayerData playerData) {
        QuestActions.ActionNode actionNode = this.getNodeAtCurrentPos();

        switch(this.getCurrentQuestStatus()) {
            case NOT_STARTED -> {
                for(QuestActions.ActionNode node : this.getPotentialStartingNodes()) {
                    QuestActions.NodeProgress progress = this.getOrStartProgress(node);
                    if(progress.isDone()) {
                        this.unregisterListeners(node, playerData);

                        this.currentNode = node.getId();

                        if(node.hasChildren()) {
                            this.registerListeners(CollectionHelper.convertCollection(node.getChildren(), this::getNodeForId), playerData);
                        }
                        break;
                    }
                    else {
                        this.registerListeners(node, playerData);
                    }
                }
            }
            case ACTIVE -> {
                if(!actionNode.hasChildren()) {
                    throw new IllegalStateException("The status of quest " + this.quest.getId() + " is active and can not be completed as there are no more actions for completion!");
                }

                for(ResourceLocation id : actionNode.getChildren()) {
                    QuestActions.ActionNode child = this.getNodeForId(id);
                    QuestActions.NodeProgress progress = this.getOrStartProgress(child);
                    if(progress.isDone() && !child.isOptional()) {
                        this.unregisterListeners(CollectionHelper.convertCollection(this.getNodeAtCurrentPos().getChildren(), this::getNodeForId), playerData);

                        this.currentNode = id;

                        if(this.getNodeAtCurrentPos().hasChildren()) {
                            this.registerListeners(CollectionHelper.convertCollection(this.getNodeAtCurrentPos().getChildren(), this::getNodeForId), playerData);
                        }

                        break;
                    }
                }
            }
        }
    }


    public Set<QuestActions.ActionNode> getChildrenForCurrentNode() {
        Set<QuestActions.ActionNode> children = new HashSet<>();
        QuestActions.ActionNode actionNode = this.getNodeAtCurrentPos();
        if(actionNode.hasChildren()) {
            actionNode.getChildren().forEach(value -> children.add(this.getNodeForId(value)));
        }

        return children;
    }


    public QuestActions.ActionNode getNodeAtCurrentPos() {
        if(this.currentNode == null) {
            QuestActions.ActionNode node = this.quest.getActions().getNodesByType(QuestActions.NodeType.START_NODE).stream().findAny().orElseThrow();
            this.currentNode = node.getId();
            return node;
        }
        else {
            return this.getNodeForId(this.currentNode);
        }
    }


    @NotNull
    private QuestActions.ActionNode getNodeForId(ResourceLocation id) {
        return this.quest.getActions().getNodeForId(id);
    }


    public Set<QuestActions.ActionNode> getPotentialStartingNodes() {
        Set<QuestActions.ActionNode> nodes = new HashSet<>();
        QuestActions actions = this.quest.getActions();
        nodes.addAll(actions.getNodesByType(QuestActions.NodeType.START_NODE));
        nodes.addAll(actions.getNodesByType(QuestActions.NodeType.POSSIBLE_START_NODE));

        return nodes;
    }


    public ProgressionQuest.Status getCurrentQuestStatus() {
        return this.getNodeAtCurrentPos().getQuestStatus();
    }


    public void registerListeners(Collection<QuestActions.ActionNode> actionNodes, PlayerData player) {
        actionNodes.forEach(actionNode -> this.registerListeners(actionNode, player));
    }


    public boolean award(QuestActions.ActionNode actionNode, String criterionName, PlayerData player) {
        boolean flag = false;
        QuestActions.NodeProgress nodeProgress = this.getOrStartProgress(actionNode);
        if(criterionName == null || nodeProgress.grantProgress(criterionName)) {
            if(criterionName == null) {
                nodeProgress.complete();
            }
            if(nodeProgress.isDone()) {
                this.unregisterListeners(actionNode, player);
                flag = true;
            }
        }

        if(flag) {
            LevelManager.getInstance().updateStatus();
        }

        return flag;
    }


    public QuestActions.NodeProgress getOrStartProgress(QuestActions.ActionNode actionNode) {
        QuestActions.NodeProgress nodeProgress = this.nodeProgress.get(actionNode);
        if(nodeProgress == null) {
            nodeProgress = new QuestActions.NodeProgress(actionNode);
            this.startProgress(actionNode, nodeProgress);
        }
        return nodeProgress;
    }


    public void unregisterListeners(QuestActions.ActionNode actionNode, PlayerData player) {
        QuestActions.NodeProgress nodeProgress = this.getOrStartProgress(actionNode);

        for(Map.Entry<String, SimpleTrigger<?>> entry : actionNode.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = nodeProgress.getCriterion(entry.getKey());
            if(criterionprogress != null && (criterionprogress.isDone() || nodeProgress.isDone())) {
                SimpleTrigger<?> trigger = entry.getValue();
                if(trigger != null) {
                    trigger.removeListener(player, new SimpleTrigger.Listener(quest, actionNode, entry.getKey()));
                }
            }
        }
    }


    private void startProgress(QuestActions.ActionNode actionNode, QuestActions.NodeProgress nodeProgress) {
        nodeProgress.updateProgress(actionNode);
        this.nodeProgress.putIfAbsent(actionNode, nodeProgress);
    }


    public boolean revoke(QuestActions.ActionNode actionNode, String criterionName, PlayerData player) {
        boolean flag = false;
        QuestActions.NodeProgress nodeProgress = this.getOrStartProgress(actionNode);
        if(criterionName == null || nodeProgress.revokeProgress(criterionName)) {
            if(criterionName == null) {
                nodeProgress.reset();
            }
            this.registerListeners(actionNode, player);
            LevelManager.getInstance().updateStatus();
            flag = true;
        }

        return flag;
    }


    public void registerListeners(QuestActions.ActionNode actionNode, PlayerData player) {
        QuestActions.NodeProgress nodeProgress = this.getOrStartProgress(actionNode);
        if(!nodeProgress.isDone()) {
            for(Map.Entry<String, SimpleTrigger<?>> entry : actionNode.getCriteria().entrySet()) {
                CriterionProgress criterionprogress = nodeProgress.getCriterion(entry.getKey());
                if(criterionprogress != null && !criterionprogress.isDone()) {
                    SimpleTrigger<?> trigger = entry.getValue();
                    if(trigger != null) {
                        trigger.addListener(player, new SimpleTrigger.Listener(quest, actionNode, entry.getKey()));
                    }
                }
            }
        }
    }


    @Override //TODO: Check if correct --> probably incorrect!
    public float getPercent() {
        if(this.getCurrentQuestStatus() == ProgressionQuest.Status.NOT_STARTED) {
            return 0f;
        }
        else if(this.isDone()) {
            return 1f;
        }
        else {
            float percent = 0f;

            int counter = 0;
            for(QuestActions.ActionNode actionNode : Set.copyOf(this.nodeProgress.keySet())) {
                percent += this.getOrStartProgress(actionNode).getPercent();
                counter++;
            }

            return percent / counter;
        }
    }


    @Override
    public boolean isDone() {
        ProgressionQuest.Status currentStatus = this.getCurrentQuestStatus();
        return currentStatus == ProgressionQuest.Status.COMPLETE || currentStatus == ProgressionQuest.Status.FAILED;
    }


    @Override
    public CompoundTag saveToCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("quest", this.quest.getId().toString());
        tag.putString("currentPos", this.currentNode.toString());
        tag.put("nodeProgress", CollectionHelper.saveToNBT(this.nodeProgress, actionNode -> String.valueOf(this.quest.getActions().getIdForNode(actionNode)), QuestActions.NodeProgress::saveToNBT));

        return tag;
    }


    @Override
    public void reset() {
        this.nodeProgress.clear();
        this.currentNode = null;
    }


    @Override
    public void complete() {
        this.currentNode = this.getLastNodeId(true);

        QuestActions actions = this.quest.getActions();

        actions.forEach((id, actionNode) -> {
            if(actionNode.getQuestStatus() != ProgressionQuest.Status.FAILED) {
                this.getOrStartProgress(actionNode).complete();
            }
        });
    }


    public ResourceLocation getLastNodeId(boolean successful) {
        return this.quest.getActions().getLastNodeId(successful);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.quest.getId());
        buf.writeResourceLocation(this.currentNode);
        buf.writeMap(this.nodeProgress, (buf1, actionNode) -> buf1.writeResourceLocation(this.quest.getActions().getIdForNode(actionNode)), (buf1, nodeProgress) -> nodeProgress.toNetwork(buf1));
    }


    public void stopListening(PlayerData player) {
        this.unregisterListeners(this.nodeProgress.keySet(), player);
    }


    @Override
    public ProgressionQuest getProgressable() {
        return this.quest;
    }


    public void unregisterListeners(Collection<QuestActions.ActionNode> actionNodes, PlayerData player) {
        actionNodes.forEach(actionNode -> this.unregisterListeners(actionNode, player));
    }
}
