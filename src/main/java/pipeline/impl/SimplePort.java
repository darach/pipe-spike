/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl;

import com.hazelcast.jet.Edge;
import pipeline.Pipeline;
import pipeline.Port;

import javax.annotation.Nonnull;

/*package-local*/ class SimplePort implements Port {
    public SimplePort(Pipeline parent, String name, int ordinal, boolean isRequired) {
        this.container = parent;
        this.name = name;
        this.ordinal = ordinal;
        this.isRequired = isRequired;
    }

    public static SimplePort newPort(@Nonnull Pipeline parent, Edge edge, String name) {
        return new SimplePort(parent,name,edge.getSourceOrdinal(),true);
    }

    private Pipeline container;
    private String name;
    private int ordinal;
    protected boolean isRequired;

    public Pipeline getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
