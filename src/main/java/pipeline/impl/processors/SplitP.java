/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl.processors;

import com.hazelcast.jet.AbstractProcessor;
import com.hazelcast.jet.Processor;
import com.hazelcast.jet.function.DistributedSupplier;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Non-causal branch operation based on jet primitive emit([ordinals],item)
 * used directly and non-chaining
 */
public class SplitP extends AbstractProcessor implements Serializable {
    private long serialVersionUid = 1L;

    private List<String> chain;

    public SplitP(@Nonnull List<String> vertices) {
        this.chain = vertices;
    }

    @Override
    public boolean isCooperative() {
        return false;
    }

    @Override
    protected boolean tryProcess0(@Nonnull Object item) throws Exception {
        emit(IntStream.rangeClosed(0, chain.size()).toArray(), item);
        return true;
    }

    @Override
    protected boolean tryProcess1(@Nonnull Object item) throws Exception {
        throw new IllegalStateException("This should not happen");
    }

    public static DistributedSupplier<Processor> newProcessor(List<String> chain) {
        return () -> new SplitP(chain);
    }
}
