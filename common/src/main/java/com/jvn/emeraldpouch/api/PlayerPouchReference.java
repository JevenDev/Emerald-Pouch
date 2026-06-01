package com.jvn.emeraldpouch.api;

/**
 * Public reference to a pouch held by a player inventory, Curios slot, or Accessories belt slot.
 */
public record PlayerPouchReference(Type type, int slot) {
    public enum Type {
        INVENTORY,
        CURIOS,
        ACCESSORIES_BELT
    }
}
