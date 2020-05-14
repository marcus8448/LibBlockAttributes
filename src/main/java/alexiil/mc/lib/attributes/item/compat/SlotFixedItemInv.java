/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package alexiil.mc.lib.attributes.item.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SlotFixedItemInv extends Slot {

    public final FixedItemInv inv;
    public final int slotIndex;
    private final InventoryFixedWrapper wrapper;

    private ItemStack forcedClientStackOverride = ItemStack.EMPTY;

    public SlotFixedItemInv(ScreenHandler container, FixedItemInv inv, int slotIndex, int x, int y) {
        super(new InventoryFixedWrapper(inv) {
            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return container.canUse(player);
            }

        }, slotIndex, x, y);
        this.inv = inv;
        this.slotIndex = slotIndex;
        this.wrapper = (InventoryFixedWrapper) this.inventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return inv.isItemValidForSlot(slotIndex, stack);
    }

    @Override
    public int getMaxStackAmount(ItemStack stack) {
        return inv.getMaxAmount(slotIndex, stack);
    }

    @Override
    public void setStack(ItemStack stack) {
        if (wrapper.softSetInvStack(slotIndex, stack)) {
            markDirty();
            if (isClient()) {
                forcedClientStackOverride = stack;
            }
        } else {
            if (isClient()) {
                forcedClientStackOverride = stack;
            } else {
                throw new IllegalStateException("You cannot set ");
            }
        }
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack taken = super.takeStack(amount);
        if (isClient()) {
            forcedClientStackOverride = wrapper.getStack(slotIndex);
        }
        return taken;
    }

    private static boolean isClient() {
        // TODO: A more useful test to determine if this slot really is on the client and not the server.
        return true;
    }

    @Override
    public ItemStack getStack() {
        if (!forcedClientStackOverride.isEmpty()) {
            return forcedClientStackOverride;
        }
        return super.getStack();
    }
}
