package pipeline.util;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.Edge;
import pipeline.BindingContext;
import pipeline.Pipeline;
import pipeline.impl.SimplePort;

import javax.annotation.Nonnull;
import java.util.Properties;
import java.util.function.Function;

public class FakeValidatingPipeline implements Pipeline {
    private final Pipeline wrapped;
    private final boolean isWellFormed;
    private final boolean isValid;

    public FakeValidatingPipeline(Pipeline wrapped, boolean isWellFormed, boolean isValid) {
        this.wrapped = wrapped;
        this.isWellFormed = isWellFormed;
        this.isValid = isValid;
    }

    @Override
    public String name() {
        return wrapped.name();
    }

    @Override
    public String path() {
        return wrapped.path();
    }

    @Override
    public Properties properties() {
        return wrapped.properties();
    }

    @Override
    public boolean isRoot() {
        return wrapped.isRoot();
    }

    @Override
    public DAG dag() {
        return wrapped.dag();
    }

    @Override
    public Pipeline register(@Nonnull String name, @Nonnull Pipeline p) {
        return wrapped.register(name, p);
    }

    @Override
    public BindingContext bind(@Nonnull String from, @Nonnull String to) {
        return wrapped.bind(from, to);
    }

    @Override
    public boolean isWellFormed() {
        return wrapped.isWellFormed() && isWellFormed;
    }

    @Override
    public boolean isValid() {
        return wrapped.validate() && validate();
    }

    @Override
    public boolean validate() {
        return isValid;
    }

    @Override
    public Pipeline parent() {
        return this;
    }

    @Override
    public SimplePort connect(@Nonnull String fromVertex, @Nonnull String toVertex) {
        return null;
    }

    @Override
    public SimplePort connect(@Nonnull String fromVertex, @Nonnull String toVertex, Function<Edge, Edge> edgeModifier) {
        return null;
    }

    @Override
    public SimplePort connect(@Nonnull String fromVertex, int fromOrdinal, @Nonnull String toVertex, int toOrdinal, Function<Edge, Edge> edgeModifier) {
        return null;
    }
}
