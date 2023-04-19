package de.thedead2.progression_reloaded.asm;

import de.thedead2.progression_reloaded.asm.helpers.ASMHelper.ObfType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.InsnList;

import static de.thedead2.progression_reloaded.asm.AbstractASM.ASMType.VISITOR;

public abstract class AbstractASM {
    public enum ASMType {
        VISITOR, OVERRIDE, TRANSFORM;
    }

    //Return true if the class name is valid
    public abstract boolean isClass(String name);

    //Return whether this class is applying this type of asm
    public boolean isValidASMType(ASMType type) {
        return type == VISITOR;
    }

    //Only Called when the ASMType is VISITOR
    public ClassVisitor newInstance(String name, ClassWriter writer) {
        return newInstance(writer);
    }

    //Only Called when the ASMType is VISITOR
    public ClassVisitor newInstance(ClassWriter writer) {
        return null;
    }

    //Only called when the ASMType is OVERRIDE
    public String[] getMethodNameAndDescription() {
        return new String[0];
    }

    //Only called when the ASMType is OVERRIDE
    public void addInstructions(ObfType type, InsnList list) {}

    //Only called when the ASMType is TRANSFORM
    public byte[] transform(byte[] modified) {
        return modified;
    }
}