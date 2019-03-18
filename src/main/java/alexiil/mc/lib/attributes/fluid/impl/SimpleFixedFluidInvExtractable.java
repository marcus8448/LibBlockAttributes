package alexiil.mc.lib.attributes.fluid.impl;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.IFixedFluidInv;
import alexiil.mc.lib.attributes.fluid.IFluidExtractable;
import alexiil.mc.lib.attributes.fluid.filter.IFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

public final class SimpleFixedFluidInvExtractable implements IFluidExtractable {

    private final IFixedFluidInv inv;

    /** Null means that this can extract from any of the tanks. */
    private final int[] tanks;

    public SimpleFixedFluidInvExtractable(IFixedFluidInv inv, int[] tanks) {
        this.inv = inv;
        this.tanks = tanks;
    }

    @Override
    public FluidVolume attemptExtraction(IFluidFilter filter, int maxAmount, Simulation simulation) {
        if (maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount cannot be negative! (was " + maxAmount + ")");
        }
        FluidVolume fluid = FluidKeys.EMPTY.withAmount(0);
        if (maxAmount == 0) {
            return fluid;
        }
        if (tanks == null) {
            for (int t = 0; t < inv.getTankCount(); t++) {
                FluidVolume invFluid = inv.getInvFluid(t);
                if (invFluid.isEmpty() || !filter.matches(invFluid.fluidKey)) {
                    continue;
                }
                invFluid = invFluid.copy();
                FluidVolume addable = invFluid.split(maxAmount);
                FluidVolume merged = FluidVolume.merge(fluid, addable);
                if (merged != null && inv.setInvFluid(t, invFluid, simulation)) {
                    maxAmount -= addable.getAmount();
                    fluid = merged;
                    assert maxAmount >= 0;
                    if (maxAmount <= 0) {
                        return fluid;
                    }
                }
            }
        } else {
            for (int t : tanks) {
                // Copy of above
                FluidVolume invFluid = inv.getInvFluid(t);
                if (invFluid.isEmpty() || !filter.matches(invFluid.fluidKey)) {
                    continue;
                }
                invFluid = invFluid.copy();
                FluidVolume addable = invFluid.split(maxAmount);
                FluidVolume merged = FluidVolume.merge(fluid, addable);
                if (merged != null && inv.setInvFluid(t, invFluid, simulation)) {
                    maxAmount -= addable.getAmount();
                    fluid = merged;
                    assert maxAmount >= 0;
                    if (maxAmount <= 0) {
                        return fluid;
                    }
                }
            }
        }

        return fluid;
    }
}