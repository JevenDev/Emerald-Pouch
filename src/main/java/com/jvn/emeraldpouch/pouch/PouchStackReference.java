package com.jvn.emeraldpouch.pouch;

public record PouchStackReference(Type type, int slot) {
    public static PouchStackReference inventory(int slot) {
        return new PouchStackReference(Type.INVENTORY, slot);
    }

    public static PouchStackReference curios(int slot) {
        return new PouchStackReference(Type.CURIOS, slot);
    }

    public enum Type {
        INVENTORY,
        CURIOS;

        public int networkId() {
            return ordinal();
        }

        public static Type fromNetworkId(int id) {
            if (id == CURIOS.networkId()) {
                return CURIOS;
            }
            return INVENTORY;
        }
    }
}
