package org.brandonhaynes.pipegen.optimization.schemaInference;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

import static org.brandonhaynes.pipegen.support.VectorFactories.*;

public class MultiColumnCompositeVectorTests {
    @Test
    public void IntFloatCompositeVector() throws Exception {
        CompositeVector composite = new CompositeVector(createIntegerVector(), createFloatVector());

        composite.allocateNew(-1, 3);

        composite.getMutator().set(10);
        composite.getMutator().set(100.5);
        composite.getMutator().set(20);
        composite.getMutator().set(200.5);
        composite.getMutator().set(30);
        composite.getMutator().set(300.5);

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(10));
        assert(accessor.get(1, 0).equals(100.5));

        assert(accessor.get(0, 1).equals(20));
        assert(accessor.get(1, 1).equals(200.5));

        assert(accessor.get(0, 2).equals(30));
        assert(accessor.get(1, 2).equals(300.5));
    }

    @Test
    public void IntStringFloatCompositeVector() throws Exception {
        CompositeVector composite = new CompositeVector(createIntegerVector(),
                                                        createStringVector(),
                                                        createFloatVector());
        composite.allocateNew(128, 3);

        composite.getMutator().set(10);
        composite.getMutator().set("a");
        composite.getMutator().set(100.5);
        composite.getMutator().set(20);
        composite.getMutator().set("b");
        composite.getMutator().set(200.5);
        composite.getMutator().set(30);
        composite.getMutator().set("c");
        composite.getMutator().set(300.5);

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(10));
        assert(accessor.get(1, 0).toString().equals("a"));
        assert(accessor.get(2, 0).equals(100.5));

        assert(accessor.get(0, 1).equals(20));
        assert(accessor.get(1, 1).toString().equals("b"));
        assert(accessor.get(2, 1).equals(200.5));

        assert(accessor.get(0, 2).equals(30));
        assert(accessor.get(1, 2).toString().equals("c"));
        assert(accessor.get(2, 2).equals(300.5));
    }

    @Test
    public void SetAugmentedString() throws Exception {
        CompositeVector composite = new CompositeVector(createIntegerVector(),
                                                        createFloatVector(),
                                                        createStringVector());
        composite.allocateNew(100, 3);
        composite.getMutator().set(new AugmentedString(10, ',', 100.5, ",", "a", '\n'));
        composite.getMutator().set(new AugmentedString(20, ',', 200.5, ",", "b", "\n"));
        composite.getMutator().set(new AugmentedString(30, ',', 300.5, ",", "c", '\n'));

        CompositeVector.Accessor accessor = composite.getAccessor();
        assert(accessor.get(0, 0).equals(10));
        assert(accessor.get(1, 0).equals(100.5));
        assert(accessor.get(2, 0).toString().equals("a"));

        assert(accessor.get(0, 1).equals(20));
        assert(accessor.get(1, 1).equals(200.5));
        assert(accessor.get(2, 1).toString().equals("b"));

        assert(accessor.get(0, 2).equals(30));
        assert(accessor.get(1, 2).equals(300.5));
        assert(accessor.get(2, 2).toString().equals("c"));
    }
}
