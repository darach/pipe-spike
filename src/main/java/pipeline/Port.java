/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

/**
 * A pipeline, template, or operator Port is a logical connection
 * point in the pipeline.
 *
 * Currently this is under-defined. This is used for internal pipeline
 * connections within operators.
 *
 * At this time input and output ports are not defined.
 */
public interface Port {
    /**
     * Get the owning pipeline
     * @return The owner
     */
    Pipeline getContainer();

    /**
     * Get the name of this port
     * @return The name
     */
    String getName();

    /**
     * Get the ordinal this port consumes
     * @return The ordinal
     */
    int getOrdinal();

    /**
     * Is connecting this port required or optional
     * @return true if connecting the port is required, false otherwise
     */
    boolean isRequired();
}
