/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline;

import com.hazelcast.jet.Processor;
import com.hazelcast.jet.ProcessorMetaSupplier;
import com.hazelcast.jet.ProcessorSupplier;
import com.hazelcast.jet.function.DistributedSupplier;

/**
 * Encapsulates a branch operation builder. A branch operation is
 * any operation that branches an upstream pipeline into multiple
 * downstream pipelines, copying items from the inputs onto
 * multiple outputs.
 *
 */
public interface BranchOperator extends Operator {
    /**
     * Defines a named branch with the given processor
     *
     * @param name The branch name
     * @param processor The processor for this branch
     * @return The updated builder
     */
    BranchOperator branch(String name, DistributedSupplier<? extends Processor> processor);

    /**
     * Defines a named branch with the given processor
     *
     * @param name The branch name
     * @param processor The processor for this branch
     * @return The updated builder
     */
    BranchOperator branch(String name, ProcessorSupplier processor);

    /**
     * Defines a named branch with the given processor
     *
     * @param name The branch name
     * @param processor The processor for this branch
     * @return The updated builder
     */
    BranchOperator branch(String name, ProcessorMetaSupplier processor);
}
