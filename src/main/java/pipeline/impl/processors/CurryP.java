/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl.processors;

import com.hazelcast.jet.AbstractProcessor;

import javax.annotation.Nonnull;

/**
 * The curry processor emits an item on ordinal 0 before the same item
 * is emitted on ordinal 1.
 */
public class CurryP extends AbstractProcessor {
    public CurryP() {
        setCooperative(false);
    }
    @Override
    protected boolean tryProcess0(@Nonnull Object item) throws Exception {
        emit(new int[] {0, 1},item);
        return true;
    }

    @Override
    protected boolean tryProcess1(@Nonnull Object item) throws Exception {
        throw new IllegalStateException("Bad by construction");
    }
}
