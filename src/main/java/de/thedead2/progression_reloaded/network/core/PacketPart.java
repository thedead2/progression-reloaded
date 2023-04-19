package de.thedead2.progression_reloaded.network.core;

public enum PacketPart {
    SEND_HASH(true), REQUEST_SIZE(false), SEND_SIZE(true), REQUEST_DATA(false), SEND_DATA(true), SEND_TO_SERVER(true);
    
    private final boolean sends;
    
    private PacketPart(boolean sends) {
        this.sends = sends;
    }

    public boolean sends() {
        return sends;
    }
}