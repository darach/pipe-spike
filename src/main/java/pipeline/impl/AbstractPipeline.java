/**
 * Copyright (C) Darach Ennis 2017. See LICENSE.txt at the top level of the project
 */

package pipeline.impl;

import java.util.*;
import java.util.function.Function;

import com.hazelcast.jet.*;
import com.hazelcast.util.Preconditions;

import pipeline.BindingContext;
import pipeline.Pipeline;
import pipeline.Port;

import javax.annotation.Nonnull;


/**
 * Default boilerplate implementation of a pipeline
 */
public abstract class AbstractPipeline implements BindingContext, Pipeline {
    protected final String name;
    final String path;
    final Properties props;
    protected DAG dag;
    final Map<String,Pipeline> templates = new HashMap<>();
    final BindingContext self;

    protected AbstractPipeline(@Nonnull final String name) {
        this(name, new DAG(), new Properties());
    }

    AbstractPipeline(@Nonnull final String name, @Nonnull DAG dag, @Nonnull final Properties props) {
        this.name = name;
        this.props = props;
        this.path = name;
        this.dag = dag;
        this.self = new AbstractBindingContext(this,this,name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Properties properties() {
        return props;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public boolean isRoot() {
        return name.equals(path());
    }

    @Override
    public DAG dag() {
        return dag;
    }

    @Override
    public Pipeline register(@Nonnull String name, @Nonnull Pipeline p) {
        Preconditions.checkFalse(templates.containsKey(name), "Cannot add multiple stages with the same name");

        final Pipeline maybeAdded = templates.putIfAbsent(name, p);

        if ( maybeAdded != null ) {
            throw new IllegalArgumentException("Cannot replace pipeline stage '" + name + "' as it already exists in this pipeline");
        }

        return maybeAdded;
    }

    @Override
    public BindingContext bind(String fromPipeline, String alias) {
        final Pipeline fp = templates.get(fromPipeline);

        checkIsBindable(fromPipeline, fp);

        return new AbstractBindingContext(this, fp, alias);
    }

    private void checkIsBindable(final String fromPipeline, final Pipeline fp) {
        if ( !fp.isWellFormed() ) {
            throw new IllegalArgumentException("Pipeline " + fromPipeline + " is not well formed");
        }
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean isWellFormed() {
        // NOTE HACK Force validation via DAG.iterator() as validate() is package private
        dag.iterator();
        return true;
    }

    @Override
    public boolean isValid() {
        for (final Pipeline stage : templates.values()) {
            if ( "$".equals(stage.name()) ) continue;
            if ( !stage.isWellFormed() || !stage.isValid() ) return false;
            if ( !stage.validate() ) return false;
        }

        return isWellFormed() && validate();
    }

    @Override
    public Pipeline parent() {
        return this;
    }

    @Override
    public Port connect(@Nonnull String fromVertex, @Nonnull String toVertex) {
        return self.connect(fromVertex,0,toVertex,0,(e) -> e);
    }

    @Override
    public Port connect(@Nonnull String fromVertex, @Nonnull String toVertex, Function<Edge, Edge> edgeModifier) {
        return self.connect(fromVertex,0,toVertex,0,edgeModifier);
    }

    @Override
    public Port connect(@Nonnull String fromVertex, int fromOrdinal, @Nonnull String toVertex, int toOrdinal, Function<Edge, Edge> edgeModifier) {
        return self.connect(fromVertex,fromOrdinal,toVertex,toOrdinal, edgeModifier);
    }
}
