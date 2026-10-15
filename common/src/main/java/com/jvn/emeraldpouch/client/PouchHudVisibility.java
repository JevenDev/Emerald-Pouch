package com.jvn.emeraldpouch.client;

public final class PouchHudVisibility {
    private PouchHudVisibility() {
    }

    public static boolean isVisible(boolean spectator, boolean hideGui) {
        return !spectator && !hideGui;
    }
}
