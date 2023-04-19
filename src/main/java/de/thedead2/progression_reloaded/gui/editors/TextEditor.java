package de.thedead2.progression_reloaded.gui.editors;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;

import java.util.HashMap;

public class TextEditor {
    private HashMap<ITextEditable, Integer> cachedPosition = new HashMap();
    private ITextEditable editable = null;
    private boolean isTextEditing;
    private int position;
    private boolean white;
    private int tick;

    public boolean setEditable(ITextEditable editable) {
        if (this.editable != editable) {
            this.position = editable.getTextField().length();
        }

        this.isTextEditing = true;
        this.editable = editable;

        return true;
    }
    
    public ITextEditable getEditable() {
        return editable;
    }

    public void clearEditable() {
        this.isTextEditing = false;
        this.editable = null;
        this.position = 0;
    }
    
    public boolean isEditing() {
        return isTextEditing && editable != null;
    }

    public void keyTyped(char character, int key) {
        if (isTextEditing && editable != null) {
            ///Reset is just in case
            //setEditable(editable);
            if (key == 203) {
                cursorLeft(1);
            } else if (key == 205) {
                cursorRight(1);
            } else if (character == 22) {
                add(GuiScreen.getClipboardString());
            } else if (key == 14) {
                delete(-1);
            } else if (key == 211) {
                delete(0);
            } else if (key == 28 || key == 156) {
                add("\n");
            } else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                add(Character.toString(character));
            }
            
            //Update the cache
            cachedPosition.put(editable, position);
        }
    }

    public String getText(ITextEditable editable) {
        if (!isTextEditing || editable != this.editable)
            return editable.getTextField();
        else {
            String field = editable.getTextField();
            updateColor();

            if (white) {
                return new StringBuilder(field).insert(Math.min(position, field.length()), "_").toString() + " ";
            } else {
                return new StringBuilder(field).insert(Math.min(position, field.length()), "_").toString() + " ";
            }
        }
    }

    private boolean fixPosition() {
        if (editable == null) return false;
        if (position > editable.getTextField().length()) {
            position = editable.getTextField().length();
        }

        return true;
    }

    private void updateColor() {
        if (!fixPosition()) return;
        tick++;
        if (tick % 60 == 0) {
            if (white) {
                white = false;
            } else {
                white = true;
            }
        }
    }

    private void cursorLeft(int count) {
        int left = position - count;
        if (left < 0) {
            position = 0;
        } else position = left;
        if (!fixPosition()) return;
    }

    private void cursorRight(int count) {
        if (!fixPosition()) return;
        String text = editable.getTextField();
        int right = position + count;
        if (right > text.length()) {
            position = text.length();
        } else position = right;
    }

    private void add(String string) {
        if (!fixPosition()) return;
        String text = editable.getTextField();
        StringBuilder builder = new StringBuilder(text);
        text = builder.insert(position, string).toString();
        editable.setTextField(text);
        cursorRight(string.length());
    }

    private void delete(int count) {
        String text = editable.getTextField();
        if ((count < 0 && position > 0) || (count >= 0 && position + count < text.length())) {
            StringBuilder builder = new StringBuilder(text);
            int delete = position + count;
            if (delete > editable.getTextField().length()) {
                delete = editable.getTextField().length();
            }

            text = builder.deleteCharAt(delete).toString();
            editable.setTextField(text);
            if (count < 0) cursorLeft(-count);
            else if (count >= 0) cursorRight(count);
        }
    }
    
    //If it's null
    public void setText(String string) {
        if (editable != null) {
            editable.setTextField("");
            position = editable.getTextField().length();
        }
    }
}