/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl.operators;

import com.hazelcast.jet.*;
import com.hazelcast.jet.function.DistributedSupplier;
import com.hazelcast.jet.processor.Processors;
import pipeline.BranchOperator;
import pipeline.impl.processors.CurryP;
import pipeline.impl.templates.DagTemplate;
import pipeline.Pipeline;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

import static com.hazelcast.jet.Edge.from;
import static pipeline.PipelineUtil.passthrough;

/**
 * The Causal Split Operator guarantees that an item is emitted to a branch
 * before being propagated to proceeding branches in a branching split operation.
 *
 */
public class CausalSplitOperator extends DagTemplate implements BranchOperator {
    private long count = 0;
    private boolean isWellFormed;
    private boolean isValid;
    List<String> chain = new LinkedList<>();

    CausalSplitOperator(@Nonnull String name) {
        super(name, new DAG());
    }

    public CausalSplitOperator branch(String name, DistributedSupplier<? extends Processor> processor) {
        dag.newVertex(fqon(name), processor).localParallelism(1);
        chain.add(fqon(name));
        count++;
        return this;
    }

    public CausalSplitOperator branch(String name, ProcessorSupplier processor) {
        dag.newVertex(fqon(name), processor).localParallelism(1);
        chain.add(fqon(name));
        count++;
        return this;
    }

    public CausalSplitOperator branch(String name, ProcessorMetaSupplier processor) {
        dag.newVertex(fqon(name), processor).localParallelism(1);
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

        Vertex pivot, first = pivot = dag.newVertex("split:" + name + ":first", () -> new CurryP()).localParallelism(1);
        Vertex last = dag.newVertex("split:" + name + ":last", passthrough()).localParallelism(1);

        if (!chain.isEmpty()) {
            last = get(chain.get(chain.size() - 1));

            for (String branchVertexName : chain) {
                Vertex branch = get(branchVertexName);
                // TODO Explore the relevant subset of edge config relevant here
                final Vertex fork = dag.newVertex(pivot.getName() + "->" + branch.getName(), () -> new CurryP()).localParallelism(1);
                dag.edge(from(pivot, 0).to(branch, 0).priority(0).isolated().priority(0));
                dag.edge(from(pivot, 1).to(fork, 0).priority(1).isolated().priority(1));

                pivot = fork;
            }
        }

        final Vertex in = dag.newVertex("split:" + name + ":in", passthrough()).localParallelism(1);
        final Vertex drop = dag.newVertex("split:" + name + ":drop", Processors.noop());
        final Vertex out = dag.newVertex("split:" + name + ":out", passthrough()).localParallelism(1);
        dag.edge(from(in, 0).to(first, 0).isolated().priority(2));
        dag.edge(from(last, 0).to(drop)); // HACK Dodge ordinal ordering enforced by jet
        dag.edge(from(last, 1).to(out, 0).isolated().buffered().priority(3));

        // TODO Explore further checks for validity
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
