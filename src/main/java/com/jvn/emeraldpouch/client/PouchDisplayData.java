package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import java.util.Locale;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record PouchDisplayData(long totalEmeraldEquivalent, int pouchCount) {
    public static final PouchDisplayData EMPTY = new PouchDisplayData(0L, 0);

    public static PouchDisplayData fromPlayerInventory(Player player) {
        if (player == null) {
            return EMPTY;
        }

        Inventory inventory = player.getInventory();
        long totalEmeralds = 0L;
        int totalPouches = 0;

        for (var reference : PouchInventoryAccess.getDeterministicPouchReferences(inventory)) {
            ItemStack stack = PouchInventoryAccess.getPouchStack(inventory, reference);
            if (!PouchData.isPouchStack(stack)) {
                continue;
            }

            totalPouches++;
            totalEmeralds += PouchData.getStoredEmeraldEquivalent(stack);
        }

        if (totalPouches <= 0) {
            return EMPTY;
        }

        return new PouchDisplayData(totalEmeralds, totalPouches);
    }

    public boolean hasPouches() {
        return pouchCount > 0;
    }

    public long fullBlocksEquivalent() {
        return totalEmeraldEquivalent / 9L;
    }

    public int emeraldRemainderAfterBlocks() {
        return (int) (totalEmeraldEquivalent % 9L);
    }

    public String compactEmeraldAmount() {
        if (totalEmeraldEquivalent < 1000L) {
            return Long.toString(totalEmeraldEquivalent);
        }

        long thousands = totalEmeraldEquivalent / 1000L;
        long tenths = (totalEmeraldEquivalent % 1000L) / 100L;
        if (tenths == 0L) {
            return thousands + "k";
        }

        return thousands + "." + tenths + "k";
    }

    public String formattedEmeraldAmount() {
        return String.format(Locale.US, "%,d", totalEmeraldEquivalent);
    }

    public String formattedFullBlockEquivalent() {
        return String.format(Locale.US, "%,d", fullBlocksEquivalent());
    }
}
