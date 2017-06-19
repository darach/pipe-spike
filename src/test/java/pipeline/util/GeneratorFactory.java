package pipeline.util;

import java.io.Serializable;
import java.util.stream.Stream;

interface GeneratorFactory<T extends Serializable> extends Serializable {
    public AbstractGenerator<T> newGenerator(Stream<T> streamOf);
}
