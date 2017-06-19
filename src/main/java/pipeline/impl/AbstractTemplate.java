/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl;

import com.hazelcast.jet.DAG;

import java.util.Properties;

import pipeline.Template;

import javax.annotation.Nonnull;

/**
 * Default boilerplate implementation of a template
 */
public abstract class AbstractTemplate extends AbstractPipeline implements Template {
    AbstractTemplate(@Nonnull String name) {
        super(name, new DAG(), new Properties());
        fireDefine();
    }

    protected AbstractTemplate(@Nonnull String name, @Nonnull DAG dag, @Nonnull Properties props) {
        super(name, dag, props);
    }
}
