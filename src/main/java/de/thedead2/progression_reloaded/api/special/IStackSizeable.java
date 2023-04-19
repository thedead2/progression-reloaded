package de.thedead2.progression_reloaded.api.special;

/** Implement this on field providers, where the item 
 *  that they display should also display the stacksize */
public interface IStackSizeable {
    /** Return the stack size **/
    public int getStackSize();
}
