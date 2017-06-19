package pipeline.util;

import java.io.Serializable;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class AbstractFiniteGeneratorFactory<T extends Serializable> implements Serializable {
    private final int lp;
    private final int tp;

    AbstractFiniteGeneratorFactory(int lp, int tp) {
        this.lp = lp;
        this.tp = tp;
    }

    public int start(int addressOf) {
        return addressOf * lp;
    }

    public int end(int addressOf) {
        return (addressOf + 1) * lp;
    }

    public int localParallelism() {
        return lp;
    }

    public int totalParallelism() {
        return tp;
    }

    public AbstractGenerator<T> newGenerator(LongStream streamOf) {
        return newGenerator((Stream<T>) (streamOf.boxed())); // Specialize because erasure WTF with Stream<T> vs <Prim>Stream.boxed
    }

    public AbstractGenerator<T> newGenerator(Stream<T> streamOf) {
        return new AbstractGenerator<>(streamOf);
    }

}
