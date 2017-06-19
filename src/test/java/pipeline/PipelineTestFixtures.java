/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.processor.Sinks;
import pipeline.impl.templates.DagTemplate;
import pipeline.util.FiniteGenerator;

import static com.hazelcast.jet.Edge.between;
import static pipeline.PipelineUtil.passthrough;

public class PipelineTestFixtures {

    private PipelineTestFixtures() { }

    public static Template source(int count) {
        final DAG sourceDag = new DAG();
        sourceDag.edge(
            between(
                sourceDag.newVertex("generator", new FiniteGenerator<Long>(count)).localParallelism(1),
                sourceDag.newVertex("out", passthrough()).localParallelism(1)
            ).allToOne()
        );
        return new DagTemplate("source", sourceDag);
    }

    public static Template sink(String logFileName) {
        final DAG sinkDag = new DAG();
        sinkDag.newVertex("in", Sinks.writeFile(logFileName));
        return new DagTemplate("sink", sinkDag);
    }

    public static boolean isEven(Long x) {
        System.out.println("EVEN? " + x);
        return (x & 1) == 0;
    }

    public static boolean isOdd(Long x) {
        System.out.println("ODD? " + x);
        return (x & 1) == 1;
    }
}
