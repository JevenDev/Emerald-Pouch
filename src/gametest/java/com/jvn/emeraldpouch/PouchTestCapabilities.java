package com.jvn.emeraldpouch;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

/** Test-only stand-in for metadata attached to pouch stacks by another mod. */
public final class PouchTestCapabilities {
    private PouchTestCapabilities() {
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        if (!(event.getObject().getItem() instanceof com.jvn.emeraldpouch.item.PouchItem)) {
            return;
        }
        event.addCapability(new ResourceLocation("emeraldpouch", "test_metadata"), new ICapabilitySerializable<CompoundTag>() {
            private CompoundTag data = new CompoundTag();

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
                return LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT() {
                return data.copy();
            }

            @Override
            public void deserializeNBT(CompoundTag value) {
                data = value.copy();
            }
        });
    }
}
