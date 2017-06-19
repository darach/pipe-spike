/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

/**
 * A template is a pipeline that is well formed and valid in and of itself
 * but designed for reuse. Templates are integrated via the jet DAG API but
 * there is no reason that templates necessarily be DAGs themselves.
 *
 * In short, templates are a unit of reusability in a pipeline and are themselves
 * complete pipelines in their own right.
 */
public interface Template extends Pipeline {
    /**
     * Callback invoked by AbstractTemplate after a template has been constructed allowing
     * template subclasses to define enhanced constraints.
     *
     */
    void fireDefine();
}
