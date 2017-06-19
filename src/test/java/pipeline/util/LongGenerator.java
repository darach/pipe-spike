package pipeline.util;

import java.io.Serializable;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class LongGenerator extends AbstractGenerator<Long> implements Serializable {
    LongGenerator(Stream<Long> streamOf) {
        super(streamOf);
    }
    LongGenerator(LongStream streamOf) {
        super(streamOf.boxed());
    }

    static class LongGeneratoryFactory implements GeneratorFactory<Long> {
        public AbstractGenerator<Long> newGenerator(Stream<Long> streamOf) {
            return new LongGenerator(streamOf);
        }
    }
}
