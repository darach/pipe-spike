/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl.operators;

import com.hazelcast.jet.*;
import com.hazelcast.jet.function.DistributedSupplier;
import pipeline.BranchOperator;
import pipeline.impl.processors.SplitP;
import pipeline.impl.templates.DagTemplate;
import pipeline.Pipeline;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

import static com.hazelcast.jet.Edge.from;
import static pipeline.PipelineUtil.passthrough;

/**
 * Splits a single pipeline into multiple downstream pipelines. This
 */
class OrdinarySplitOperator extends DagTemplate implements BranchOperator {
    private long count = 0;
    private boolean isWellFormed;
    private boolean isValid;
    List<String> chain = new LinkedList<>();

    OrdinarySplitOperator(@Nonnull String name) {
        super(name, new DAG());
    }

    public OrdinarySplitOperator branch(String name, DistributedSupplier<? extends Processor> processor) {
        dag().newVertex(fqon(name), processor).localParallelism(1);
        chain.add(fqon(name));
        count++;
        return this;
    }

    public OrdinarySplitOperator branch(String name, ProcessorSupplier processor) {
        dag().newVertex(fqon(name), processor).localParallelism(1);
        chain.add(fqon(name));
        count++;
        return this;
    }

    public OrdinarySplitOperator branch(String name, ProcessorMetaSupplier processor) {
        dag().newVertex(fqon(name), processor).localParallelism(1);
        chain.add(fqon(name));
        count++;
        return this;
    }

    public Pipeline complete() {
        isWellFormed = true;

        if (count == 0) {
            isValid = false;
            return this;
        }

        Vertex split = dag().newVertex("split:chain", SplitP.newProcessor(chain)).localParallelism(1);


        final Vertex in = dag.newVertex("split:" + name + ":in", passthrough()).localParallelism(1);
        final Vertex out = dag.newVertex("split:" + name + ":out", passthrough()).localParallelism(1);

        dag.edge(from(in, 0).to(split, 0).isolated());
        for (int i = 0; i < chain.size(); i++) {
            String n = chain.get(i);
            //String v = get(n) == null ? null : get(n).getName();
            dag.edge(from(split, i).to(get(chain.get(i))).isolated());
        }
        dag.edge(from(split, chain.size()).to(out, 0).isolated());

        isValid = true;

        // We have multiple vertices
        return this;
    }


    private String fqon(String name) {
        return "split:" + name + ":" + count;
    }

    @Override
    public boolean isWellFormed() {
        return isWellFormed;
    }

    @Override
    public boolean validate() {
        return isValid;
    }
}
