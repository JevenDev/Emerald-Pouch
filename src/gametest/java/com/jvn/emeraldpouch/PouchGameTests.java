package com.jvn.emeraldpouch;

import com.jvn.emeraldpouch.recipe.PouchCraftingChecks;
import com.jvn.emeraldpouch.pouch.PouchPickupChecks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("emeraldpouch")
@PrefixGameTestTemplate(false)
public final class PouchGameTests {
    @GameTest(template = "empty")
    public static void normalCrafting(GameTestHelper helper) { PouchCraftingChecks.crafting(helper, false); }
    @GameTest(template = "empty")
    public static void shiftClickCrafting(GameTestHelper helper) { PouchCraftingChecks.crafting(helper, true); }
    @GameTest(template = "empty")
    public static void fullInventory(GameTestHelper helper) { PouchCraftingChecks.fullInventory(helper); }
    @GameTest(template = "empty")
    public static void nbtAndValidation(GameTestHelper helper) { com.jvn.emeraldpouch.recipe.PouchUpgradeNbtChecks.check(helper); }
    @GameTest(template = "empty")
    public static void hudModes(GameTestHelper helper) { PouchCraftingChecks.hudModes(helper); }
    @GameTest(template = "empty")
    public static void autoPickup(GameTestHelper helper) { PouchPickupChecks.autoPickup(helper); }
}
