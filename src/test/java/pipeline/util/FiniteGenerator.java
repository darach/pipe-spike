package pipeline.util;

import com.hazelcast.jet.ProcessorMetaSupplier;
import com.hazelcast.jet.ProcessorSupplier;
import com.hazelcast.nio.Address;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.LongStream.range;

public class FiniteGenerator<T extends Serializable> implements ProcessorMetaSupplier, Serializable {
    private final int limit;
    private transient AbstractFiniteGeneratorFactory<T> generator;

    public FiniteGenerator(int limit) {
        this.limit = limit;
    }

    @Override
    public void init(@Nonnull Context context) {
        generator = new AbstractFiniteGeneratorFactory<T>(
                context.localParallelism(),
                context.totalParallelism()
        );
    }

    @Override
    @Nonnull
    public Function<Address, ProcessorSupplier> get(@Nonnull List<Address> addresses) {
        Map<Address, ProcessorSupplier> map = new HashMap<>();
        for (int i = 0; i < addresses.size(); i++) {
            final Address address = addresses.get(i);
            final long start = generator.start(i);
            final long end = generator.end(i);
            final long mod = generator.totalParallelism();
            map.put(address, count -> range(start, end)
                    .mapToObj(index -> generate(mod, index))
                    .collect(Collectors.toList())
            );
        }
        return map::get;
    }

    private AbstractGenerator<T> generate(long mod, long index) {
        return generator.newGenerator(range(0, limit).filter(f -> f % mod == index));
    }
}
