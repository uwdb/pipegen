package org.brandonhaynes.pipegen.optimization.schemaInference;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

public class MixedColumnTests {
    @Test
    public void twoColumnsCharSeparator() throws Exception {
        List<Type> types = ColumnUtilities.inferSchema(new AugmentedString(999, ',', 55.5f, '\n'));
        assert (types.size() == 2);
        assert (types.get(0) == Long.class);
        assert (types.get(1) == Double.class);
    }

    @Test
    public void twoColumnsStringSeparator() throws Exception {
        List<Type> types = ColumnUtilities.inferSchema(new AugmentedString(999, ",", "foo", '\n'));
        assert (types.size() == 2);
        assert (types.get(0) == Long.class);
        assert (types.get(1) == String.class);
    }

    @Test
    public void threeColumns() throws Exception {
        List<Type> types = ColumnUtilities.inferSchema(new AugmentedString(999999999999999L, ",", 111d, ",", "bar", '\n'));
        assert (types.size() == 3);
        assert (types.get(0) == Long.class);
        assert (types.get(1) == Double.class);
        assert (types.get(2) == String.class);
    }

    @Test
    public void embeddedComma() throws Exception {
        List<Type> types = ColumnUtilities.inferSchema(new AugmentedString(111d, ",", "baz,qux", '\n'));
        assert (types.size() == 2);
        assert (types.get(0) == Double.class);
        assert (types.get(1) == String.class);
    }
}
