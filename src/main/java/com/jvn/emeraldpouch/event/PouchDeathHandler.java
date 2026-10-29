package com.jvn.emeraldpouch.event;

import com.jvn.emeraldpouch.pouch.PouchDeathRetention;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class PouchDeathHandler {
    private static final Map<ServerPlayer, Map<Integer, ItemStack>> ORIGINAL_SLOTS = new WeakHashMap<>();

    private PouchDeathHandler() {
    }

    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Map<Integer, ItemStack> slots = new HashMap<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (PouchDeathRetention.shouldKeep(stack)) slots.put(slot, stack.copy());
        }
        ORIGINAL_SLOTS.put(player, slots);
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Map<Integer, ItemStack> slots = ORIGINAL_SLOTS.remove(player);
        if (slots == null) return;
        var drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemStack stack = drops.next().getItem();
            if (!PouchDeathRetention.shouldKeep(stack)) continue;
            var match = slots.entrySet().stream().filter(entry -> ItemStack.matches(entry.getValue(), stack)).findFirst();
            if (match.isEmpty()) continue;
            int slot = match.get().getKey();
            slots.remove(slot);
            drops.remove();
            // retain the original slot in saved player data until respawn
            if (player.getInventory().getItem(slot).isEmpty()) {
                player.getInventory().setItem(slot, stack);
            } else {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && !event.getOriginal().isSpectator()
                && !event.getOriginal().level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            PouchDeathRetention.restoreInventory(event.getOriginal().getInventory(), event.getEntity().getInventory());
        }
    }
}
