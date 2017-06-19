/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

/**
 * An operator is a template whose structure varies depending
 * on user defined configuration.
 */
public interface Operator extends Template {

    /**
     * Signals that configuration of the operator is complete and
     * generates a pipeline based on that configuration
     *
     * @return A pipeline for the operation
     */
    Pipeline complete();
}
