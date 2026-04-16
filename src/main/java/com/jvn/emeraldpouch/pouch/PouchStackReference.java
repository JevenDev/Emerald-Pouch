package com.jvn.emeraldpouch.pouch;

public record PouchStackReference(Type type, int slot) {
    public static PouchStackReference inventory(int slot) {
        return new PouchStackReference(Type.INVENTORY, slot);
    }

    public static PouchStackReference curios(int slot) {
        return new PouchStackReference(Type.CURIOS, slot);
    }

    public static PouchStackReference accessoriesBelt(int slot) {
        return new PouchStackReference(Type.ACCESSORIES_BELT, slot);
    }

    public enum Type {
        INVENTORY,
        CURIOS,
        ACCESSORIES_BELT;

        public int networkId() {
            return ordinal();
        }

        public static Type fromNetworkId(int id) {
            if (id == ACCESSORIES_BELT.networkId()) {
                return ACCESSORIES_BELT;
            }
            if (id == CURIOS.networkId()) {
                return CURIOS;
            }
            return INVENTORY;
        }
    }
}
