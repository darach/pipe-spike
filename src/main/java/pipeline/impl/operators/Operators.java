/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl.operators;

import pipeline.BranchOperator;

import javax.annotation.Nonnull;

/**
 * Operators
 */
public class Operators {
    private Operators() { }

    public static BranchOperator newCausalSplit(@Nonnull String name) {
        return new CausalSplitOperator(name);
    }

    public static BranchOperator newOrdinarySplit(@Nonnull String name) {
        return new OrdinarySplitOperator(name);
    }
}
