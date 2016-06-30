package org.brandonhaynes.pipegen.optimization.schemaInference;

import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

import static org.brandonhaynes.pipegen.support.VectorFactories.*;

public class SingleColumnCompositeVectorTests {
    @Test
    public void SingleVectorInteger() throws Exception {
        CompositeVector composite = new CompositeVector(createIntegerVector());

        composite.allocateNew(-1, 3);

        composite.getMutator().set(10);
        composite.getMutator().set(20);
        composite.getMutator().set(30);

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(10));
        assert(accessor.get(0, 1).equals(20));
        assert(accessor.get(0, 2).equals(30));
    }

    @Test
    public void SingleVectorFloat() throws Exception {
        CompositeVector composite = new CompositeVector(createFloatVector());

        composite.allocateNew(-1, 3);

        composite.getMutator().set(10.5);
        composite.getMutator().set(20.5);
        composite.getMutator().set(30.5);

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(10.5));
        assert(accessor.get(0, 1).equals(20.5));
        assert(accessor.get(0, 2).equals(30.5));
    }

    @Test
    public void SingleVectorString() throws Exception {
        CompositeVector composite = new CompositeVector(createStringVector());

        composite.allocateNew(100, 3);

        composite.getMutator().set("a");
        composite.getMutator().set("b");
        composite.getMutator().set("c");

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).toString().equals("a"));
        assert(accessor.get(0, 1).toString().equals("b"));
        assert(accessor.get(0, 2).toString().equals("c"));
    }

    @Test
    public void VectorGrowFromZeroTest() throws Exception {
        CompositeVector composite = new CompositeVector(createIntegerVector());

        composite.allocateNew();

        composite.getMutator().set(1);
        composite.getMutator().set(2);
        composite.getMutator().set(3);
        composite.getMutator().set(4); // Grow vector

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(1));
        assert(accessor.get(0, 1).equals(2));
        assert(accessor.get(0, 2).equals(3));
        assert(accessor.get(0, 3).equals(4));
    }

    @Test
    public void VectorGrowTest() throws Exception {
        CompositeVector composite = new CompositeVector(createIntegerVector());

        composite.allocateNew(-1, 3);

        composite.getMutator().set(1);
        composite.getMutator().set(2);
        composite.getMutator().set(3);
        composite.getMutator().set(4); // Grow vector

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(1));
        assert(accessor.get(0, 1).equals(2));
        assert(accessor.get(0, 2).equals(3));
        assert(accessor.get(0, 3).equals(4));
    }
}
