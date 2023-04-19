package de.thedead2.progression_reloaded.gui.fields;

import de.thedead2.progression_reloaded.api.IFieldRegistry;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import de.thedead2.progression_reloaded.api.gui.Position;

public class FieldRegistry implements IFieldRegistry {
    @Override
    public IField getItemPreview(Object provider, String string, int x, int y, float scale) {
        return new ItemFilterFieldPreview(string, provider, x, y, scale);
    }

    @Override
    public IField getItem(Object provider, String string, int x, int y, float scale) {
        return new ItemField(string, provider, x, y, scale);
    }

    @Override
    public IField getFilter(Object provider, String name) {
        return new ItemFilterField(name, provider);
    }

    @Override
    public IField getBoolean(Object provider, String name) {
        return new BooleanField(name, provider);
    }

    @Override
    public IField getToggleBoolean(Object provider, String booleanName, String stringName) {
        Position position = provider instanceof IRewardProvider ? Position.TOP : Position.BOTTOM;
        return new TextFieldHideable(stringName, provider, position).setBooleanField(new BooleanFieldHideable(booleanName, provider));
    }

    @Override
    public IField getText(Object provider, String name) {
        Position position = provider instanceof IRewardProvider ? Position.TOP : Position.BOTTOM;
        return new TextField(name, provider, position);
    }
}
