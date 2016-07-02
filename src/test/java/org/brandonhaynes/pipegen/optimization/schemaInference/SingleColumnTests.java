package org.brandonhaynes.pipegen.optimization.schemaInference;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.junit.Test;

import java.util.List;

public class SingleColumnTests {
    @Test
    public void singleIntegerColumn() throws Exception {
        List<Class<?>> types = ColumnUtilities.inferSchema(new AugmentedString(999, '\n'));
        assert (types.size() == 1);
        assert (types.get(0) == Long.class);
    }

    @Test
    public void singleLongColumn() throws Exception {
        List<Class<?>> types = ColumnUtilities.inferSchema(new AugmentedString(999999999999999L, '\n'));
        assert (types.size() == 1);
        assert (types.get(0) == Long.class);
    }

    @Test
    public void singleFloatColumn() throws Exception {
        List<Class<?>> types = ColumnUtilities.inferSchema(new AugmentedString(999.5f, '\n'));
        assert (types.size() == 1);
        assert (types.get(0) == Double.class);
    }

    @Test
    public void singleDoubleColumn() throws Exception {
        List<Class<?>> types = ColumnUtilities.inferSchema(new AugmentedString(999999999999999.5d, '\n'));
        assert (types.size() == 1);
        assert (types.get(0) == Double.class);
    }

    @Test
    public void singleStringColumn() throws Exception {
        List<Class<?>> types = ColumnUtilities.inferSchema(new AugmentedString("foo", '\n'));
        assert (types.size() == 1);
        assert (types.get(0) == String.class);
    }
}
