package pipeline.impl.templates;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.Vertex;
import pipeline.impl.SimplePort;
import pipeline.impl.AbstractTemplate;

import javax.annotation.Nonnull;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Defines a template in terms of an existing jet DAG
 */
public class DagTemplate extends AbstractTemplate {

        public DagTemplate(@Nonnull String name, @Nonnull DAG ref) {
            this(name, new Properties(), ref);
        }

        DagTemplate(@Nonnull String name, @Nonnull Properties props, @Nonnull DAG ref) {
            super(name, ref, props);
            this.dag = ref;
        }

        @Override
        public void fireDefine() {
            // Nothing to do
        }
    }