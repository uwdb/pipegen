package org.brandonhaynes.pipegen.optimization.schemaInference;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.junit.Test;

import java.util.List;

public class VectorCreationTests {
    @Test
    public void twoColumnsSeparator() throws Exception {
        BufferAllocator allocator = new RootAllocator(1024);
        List<ValueVector> types = ColumnUtilities.createVectors(allocator, new AugmentedString(999, ",", "foo", '\n'));
        assert (types.size() == 2);
        assert (types.get(0).getClass() == IntVector.class);
        assert (types.get(1).getClass() == VarCharVector.class);
    }

    @Test
    public void threeColumns() throws Exception {
        BufferAllocator allocator = new RootAllocator(1024);
        List<ValueVector> types = ColumnUtilities.createVectors(allocator, new AugmentedString(999999999999999L, ",", 111d, ",", "bar", '\n'));
        assert (types.size() == 3);
        assert (types.get(0).getClass() == IntVector.class);
        assert (types.get(1).getClass() == Float8Vector.class);
        assert (types.get(2).getClass() == VarCharVector.class);
    }

    @Test
    public void embeddedComma() throws Exception {
        BufferAllocator allocator = new RootAllocator(1024);
        List<ValueVector> types = ColumnUtilities.createVectors(allocator, new AugmentedString(111d, ",", "baz,qux", '\n'));
        assert (types.size() == 2);
        assert (types.get(0).getClass() == Float8Vector.class);
        assert (types.get(1).getClass() == VarCharVector.class);
    }
}
