/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.Vertex;
import pipeline.impl.AbstractPipeline;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Properties;

/**
 * A pipeline is an abstraction built on top of DAGs that allows multiple
 * DAG templates to be assembled into a more complex DAG through composition.
 *
 */
public interface Pipeline extends BindingContext, Serializable {
    /**
     * Get the name of this pipeline
     *
     * @return
     */
    String name();

    /**
     * Get the path of this pipeline
     */
    String path();

    /**
     * Get the properties associated with this pipeline
     */
    Properties properties();

    /**
     * Is this the root node in a pipeline
     */
    boolean isRoot();

    /**
     * Get the DAG this Pipeline represents
     */
    DAG dag();

    /**
     * Create a new pipeline stage from the provided pipeline template.
     * The template is cloned into this pipeline instance.
     *
     * @param name The name of this child pipeline in the context of this ( parent ) pipeline
     * @param p The subordinate child pipeline being added
     * @return A pipeline
     */
    Pipeline register(@Nonnull final String name, @Nonnull final Pipeline p);

    default BindingContext bind(@Nonnull final String template) {
        return bind(template,null);
    }

    /**
     * Bind the sink in the from pipeline or stage to the source in
     * the to pipeline or stage.
     *
     * @param template The name of the source
     * @param alias The name of the s
     * @return
     */
    BindingContext bind(@Nonnull final String template, final String alias);

    /**
     * A pipeline is well formed if it has been configured correctly sufficient
     * in and of itself outside of the context within which the pipeline is deployed.
     */
    boolean isWellFormed();

    /**
     * A pipeline is valid if it is well formed and if all of its subordinate pipelines
     * and stages are valid, and are bound and connected sufficient for the underlying
     * Jet DAG to validate correctly.
     */
    boolean isValid();

    /**
     * Concrete implements of pipelines and stages should override validate and
     * perform any required implementation specific validation checks.
     *
     * @return True if pipeline configuration is valid, false otherwise
     */
    boolean validate();

    static Pipeline newRoot() {
        // Creates a root pipeline
        final Pipeline p = new AbstractPipeline("$") { };
        p.register("$", p); // Register pipeline logical root with itself
        return p;
    }

    default Vertex get(String name) {
        return dag().getVertex(name);
    }
}
