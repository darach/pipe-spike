/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

import java.util.concurrent.ExecutionException;

import com.hazelcast.jet.*;
import com.hazelcast.jet.processor.Processors;
import static com.hazelcast.jet.Edge.between;
import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.processor.Sinks.writeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import pipeline.impl.AbstractPipeline;
import pipeline.impl.operators.Operators;
import pipeline.impl.templates.DagTemplate;
import pipeline.util.FiniteGenerator;

import static pipeline.PipelineUtil.passthrough;
import static pipeline.PipelineUtil.vertices;

/**
 * Exploration and experiments of pipeline API
 */
public class PipelineTest {
    private static DAG JET_VALID = new DAG();

    static {
        JET_VALID.newVertex("noop", Processors.noop());
    }

    private JetInstance jet;

    @Before
    public void beforeSuite() {
        jet = Jet.newJetInstance();
    }

    @After
    public void afterSuite() {
        jet.shutdown();
    }

    @Test
    public void testNotWellFormedPipeline() {
        Pipeline identity = new AbstractPipeline("JET_VALID") { };

        try {
            identity.isWellFormed();
            fail("Should have thrown");
        } catch ( IllegalArgumentException pass) {}

        try {
            identity.isValid();
            fail("Should have thrown");
        } catch ( IllegalArgumentException pass) {}
    }

    @Test
    public void testWellFormedAndValidTrivialPipeline() {
        Pipeline trivial = new DagTemplate("wrappedIdentityDag", JET_VALID);
        assertTrue(trivial.isWellFormed());
        assertTrue(trivial.isValid());
        assertTrue(trivial.isRoot());
        assertEquals(1, vertices(trivial.dag()).size());
    }

    @Test
    public void testPipelineWithUserDefinedWellFormedness() {
        Pipeline trivial = new DagTemplate("wrappedIdentityDag", JET_VALID);
        Pipeline alwaysValid = new FakeValidatingPipeline(trivial,true,true);
        Pipeline alwaysInvalid = new FakeValidatingPipeline(trivial,false,true);

        // @NOTE Jet asserts DAG validation upon iteration of vertices and throws an
        // exception when any vertex is invalid. A directly callable validate() method
        // is package-private.
        //
        // For higher level Pipeline / Builder / Template Pattern APIs a public validate()
        // hook is preferable.
        //
        try {
            assertFalse(alwaysInvalid.isWellFormed());

        } catch ( IllegalArgumentException fail) {
            fail("Should have *NOT* have thrown. DAG is well formed but pipeline is not");
        }

        try {
            alwaysValid.isWellFormed();
        } catch ( IllegalArgumentException fail) {
            fail("Should not have thrown");
        }

        assertTrue(alwaysValid.isWellFormed());
        assertTrue(alwaysValid.isValid());
        assertTrue(alwaysValid.isRoot());
        assertEquals(1, vertices(alwaysValid.dag()).size());
    }

    @Test
    public void testPipelineWithUserDefinedValidation() {
        Pipeline trivial = new DagTemplate("trivial", JET_VALID);
        Pipeline alwaysValid = new FakeValidatingPipeline(trivial,true,true);
        Pipeline alwaysInvalid = new FakeValidatingPipeline(trivial,true,false);

        // @NOTE Jet asserts DAG validation upon iteration of vertices and throws an
        // exception when any vertex is invalid. A directly callable validate() method
        // is package-private.
        //
        // For higher level Pipeline / Builder / Template Pattern APIs a public validate()
        // hook is preferable.
        //
        try {
            assertFalse(alwaysInvalid.validate());

        } catch ( IllegalArgumentException fail) {
            fail("Should have *NOT* have thrown. DAG is valid but pipeline is not");
        }

        try {
            alwaysValid.validate();
        } catch ( IllegalArgumentException fail) {
            fail("Should not have thrown");
        }

        assertTrue(alwaysValid.isWellFormed());
        assertTrue(alwaysValid.isValid());
        assertTrue(alwaysValid.isRoot());
        assertEquals(1, vertices(alwaysValid.dag()).size());
    }

    @Test
    public void testBasicPipelineComposition() throws ExecutionException, InterruptedException {
        final DAG sourceDag = new DAG();
        sourceDag.edge(
            between(
                    sourceDag.newVertex("generator", new FiniteGenerator<Long>(100)).localParallelism(1),
                    sourceDag.newVertex("data", passthrough())
            ).isolated()
        );

        final DAG sinkDag = new DAG();
        Vertex sinkItemToEntry = sinkDag.newVertex("in", Processors.map((x) -> entry(x,x)));
        Vertex sinkEntryToMap = sinkDag.newVertex("cache-to-count", writeMap("count"));
        sinkDag.edge(between(sinkItemToEntry,sinkEntryToMap).isolated());

        Template sourceTemplate = new DagTemplate("source", sourceDag);
        Template sinkTemplate = new DagTemplate("sink", sinkDag);

        Pipeline pipe = Pipeline.newRoot();
        pipe.register("source", sourceTemplate);
        pipe.register("sink", sinkTemplate);

        // As a pipe can have many instances of a template, we need to
        // explicitly bind registered templates. If an alias is provided
        // as the second argument, it will be used in vertex naming
        //
        BindingContext source = pipe.bind("source");
        BindingContext sink = pipe.bind("sink");

        assertTrue(pipe.isWellFormed());
        assertTrue(pipe.isValid());
        assertTrue(pipe.isRoot());

        pipe.connect("$.source.data","$.sink.in");
        System.out.println(pipe.dag());

        jet.newJob(pipe.dag()).execute().get();

        assertEquals(100, jet.getMap("count").values().size());
        assertEquals(4, vertices(pipe.dag()).size());
    }

    @Test
    public void testSimplePipeline() throws ExecutionException, InterruptedException {
        // Create templates. Templates here are basically wrapped Jet DAGs
        final Template source = PipelineTestFixtures.source(10);
        final Template sink = PipelineTestFixtures.sink("smoke-tests/simple-pipeline");

        // Create a pipeline
        final Pipeline pipe = Pipeline.newRoot();

        // Register needed templates
        pipe.register("source", source);
        pipe.register("sink", sink);

        // Binding creates instances of templates in the pipeline. Templates are referenced
        // by name. If one template instance is needed, the template name is the instance
        // name, otherwise, templates should be aliased.
        // FIXME TODO - check if instances of templates have already been declared
        pipe.bind("source");
        pipe.bind("sink","sink1"); // override default namespace from 'sink' to 'sink1'

        // Create an edge betweeen the output of the source template and the input of
        // the source of the sink template
        pipe.connect("$.source.out", "$.sink1.in");

        jet.newJob(pipe.dag()).execute().get();
    }

    @Test
    public void testCausalSplitPipeline() throws ExecutionException, InterruptedException {
        // Create templates. Templates here are basically wrapped Jet DAGs
        final Template source = PipelineTestFixtures.source(100);
        final Template sink = PipelineTestFixtures.sink("smoke-tests/branching/split");

        // Configure a causal split template
        final BranchOperator branchOp = Operators.newCausalSplit("process");
        assertFalse(branchOp.isWellFormed());

        branchOp
          .branch("evens", Processors.filter(PipelineTestFixtures::isEven))
          .branch("odds", Processors.filter(PipelineTestFixtures::isOdd))
          .branch("debug", Processors.filter((x) -> {
            System.out.println("DEBUG: " + x);
            return true;
        }));

        assertFalse(branchOp.isWellFormed());
        assertFalse(branchOp.isValid());

        // Construction of the dag is deferred until done() is
        // called signaling to the Pipeline builder that it is
        // well defined and ( hopefully valid )
        //
        final Pipeline customOp = branchOp.complete();
        System.out.println(customOp.dag());
        assertTrue(branchOp.isWellFormed());
        assertTrue(branchOp.isValid()); // Confirm we have a good curry

        final Pipeline pipe = Pipeline.newRoot();

        pipe.register("source", source);
        pipe.register("process", customOp);
        pipe.register("sink", sink);

        pipe.bind("source");
        pipe.bind("process");
        pipe.bind("sink");

        pipe.connect("$.source.out", "$.process.split:process:in", (e) -> e.isolated());
        pipe.connect("$.process.split:process:out", "$.sink.in", (e) -> e.isolated());

        jet.newJob(pipe.dag()).execute().get();
    }

    @Test
    public void testOrdinarySplitPipeline() throws ExecutionException, InterruptedException {
        // Create templates. Templates here are basically wrapped Jet DAGs
        final Template source = PipelineTestFixtures.source(100);
        final Template sink = PipelineTestFixtures.sink("smoke-tests/branching/ordinary");

        // Configure a causal split template
        final BranchOperator branchOp = Operators.newOrdinarySplit("process");
        assertFalse(branchOp.isWellFormed());

        branchOp
                .branch("evens", Processors.filter(PipelineTestFixtures::isEven))
                .branch("odds", Processors.filter(PipelineTestFixtures::isOdd));

        assertFalse(branchOp.isWellFormed());
        assertFalse(branchOp.isValid());

        // Construction of the dag is deferred until done() is
        // called signaling to the Pipeline builder that it is
        // well defined and ( hopefully valid )
        //
        final Pipeline customOp = branchOp.complete();
        System.out.println(customOp.dag());
        assertTrue(branchOp.isWellFormed());
        assertTrue(branchOp.isValid()); // Confirm we have a good curry

        final Pipeline pipe = Pipeline.newRoot();

        pipe.register("source", source);
        pipe.register("process", customOp);
        pipe.register("sink", sink);

        pipe.bind("source");
        pipe.bind("process");
        pipe.bind("sink");

        pipe.connect("$.source.out", "$.process.split:process:in", (e) -> e.isolated());
        pipe.connect("$.process.split:process:out", "$.sink.in", (e) -> e.isolated());

        jet.newJob(pipe.dag()).execute().get();
    }

}
