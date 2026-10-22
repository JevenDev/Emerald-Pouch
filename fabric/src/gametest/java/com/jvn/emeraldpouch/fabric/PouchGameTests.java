package com.jvn.emeraldpouch.fabric;

import com.jvn.emeraldpouch.pouch.PouchInventoryChecks;
import com.jvn.emeraldpouch.pouch.PouchPickupChecks;
import com.jvn.emeraldpouch.recipe.PouchCraftingChecks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class PouchGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void normalCrafting(GameTestHelper helper) { PouchCraftingChecks.crafting(helper, false); }
    @GameTest(template = EMPTY_STRUCTURE)
    public void shiftClickCrafting(GameTestHelper helper) { PouchCraftingChecks.crafting(helper, true); }
    @GameTest(template = EMPTY_STRUCTURE)
    public void fullInventory(GameTestHelper helper) { PouchCraftingChecks.fullInventory(helper); }
    @GameTest(template = EMPTY_STRUCTURE)
    public void hudModes(GameTestHelper helper) { PouchCraftingChecks.hudModes(helper); }
    @GameTest(template = EMPTY_STRUCTURE)
    public void pouchCounts(GameTestHelper helper) { PouchInventoryChecks.pouchCounts(helper); }
    @GameTest(template = EMPTY_STRUCTURE)
    public void autoPickup(GameTestHelper helper) { PouchPickupChecks.autoPickup(helper); }
}
