package pipeline.util;

import com.hazelcast.jet.AbstractProcessor;
import com.hazelcast.jet.Traverser;

import java.io.Serializable;
import java.util.stream.Stream;

import static com.hazelcast.jet.Traversers.traverseStream;

class AbstractGenerator<T extends Serializable> extends AbstractProcessor implements Serializable {
    private final Traverser<T> traverser;

    AbstractGenerator(Stream<T> streamOf) {
        traverser = traverseStream(streamOf);
    }

    @Override
    public boolean complete() {
        return emitFromTraverser(traverser);
    }
}
