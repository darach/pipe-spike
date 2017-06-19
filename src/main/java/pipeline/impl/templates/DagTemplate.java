/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl.templates;

import java.util.Properties;

import com.hazelcast.jet.DAG;

import pipeline.impl.AbstractTemplate;

import javax.annotation.Nonnull;


/**
 * Defines a template in terms of an existing jet DAG
 */
public class DagTemplate extends AbstractTemplate {

        public DagTemplate(@Nonnull String name, @Nonnull DAG ref) {
            this(name, new Properties(), ref);
        }

        DagTemplate(@Nonnull String name, @Nonnull Properties props, @Nonnull DAG ref) {
            super(name, ref, props);
            this.dag = ref;
        }

        @Override
        public void fireDefine() {
            // Nothing to do
        }
    }