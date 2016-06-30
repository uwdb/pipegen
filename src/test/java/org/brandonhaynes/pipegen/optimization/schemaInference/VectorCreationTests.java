package org.brandonhaynes.pipegen.optimization.schemaInference;

import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.brandonhaynes.pipegen.utilities.CompositeVector;
import org.junit.Test;

public class VectorCreationTests {
    @Test
    public void twoColumnsSeparator() throws Exception {
        CompositeVector vector = ColumnUtilities.createVector(new AugmentedString(999, ",", "foo", '\n'));
        assert (vector.getVectors().size() == 2);
        assert (vector.getVectors().get(0).getClass() == IntVector.class);
        assert (vector.getVectors().get(1).getClass() == VarCharVector.class);
    }

    @Test
    public void threeColumns() throws Exception {
        CompositeVector vector = ColumnUtilities.createVector(new AugmentedString(999999999999999L, ",", 111d, ",", "bar", '\n'));
        assert (vector.getVectors().size() == 3);
        assert (vector.getVectors().get(0).getClass() == IntVector.class);
        assert (vector.getVectors().get(1).getClass() == Float8Vector.class);
        assert (vector.getVectors().get(2).getClass() == VarCharVector.class);
    }

    @Test
    public void embeddedComma() throws Exception {
        CompositeVector vector = ColumnUtilities.createVector(new AugmentedString(111d, ",", "baz,qux", '\n'));
        assert (vector.getVectors().size() == 2);
        assert (vector.getVectors().get(0).getClass() == Float8Vector.class);
        assert (vector.getVectors().get(1).getClass() == VarCharVector.class);
    }
}
