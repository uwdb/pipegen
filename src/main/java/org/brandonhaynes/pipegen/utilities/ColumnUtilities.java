package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.types.MaterializedField;
import org.apache.arrow.vector.types.Types;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import java.util.List;

public class ColumnUtilities {
    private static final BufferAllocator defaultAllocator =
            new RootAllocator(RuntimeConfiguration.getInstance().getBufferAllocationSize());

    public static CompositeVector createVector(AugmentedString evidence) {
        return createVector(defaultAllocator, evidence);
    }

    public static CompositeVector createVector(Class<?>[] classes) {
        return createVector(Lists.newArrayList(classes));
    }

    public static CompositeVector createVector(List<Class<?>> classes) {
        return new CompositeVector(createVectors(defaultAllocator, classes));
    }

    private static CompositeVector createVector(BufferAllocator allocator, AugmentedString evidence) {
        return new CompositeVector(createVectors(allocator, inferSchema(evidence)));
    }

    private static List<ValueVector> createVectors(BufferAllocator allocator, List<Class<?>> classes) {
        int index = 0;
        List<ValueVector> vectors = Lists.newArrayList();

        for(Class<?> clazz: classes)
            if(clazz == Long.class || clazz == IntVector.class)
                vectors.add(new IntVector(MaterializedField.create("column" + index++,
                        new Types.MajorType(Types.MinorType.INT, Types.DataMode.REQUIRED)), allocator));
            else if(clazz == Double.class || clazz == Float8Vector.class)
                vectors.add(new Float8Vector(MaterializedField.create("column" + index++,
                        new Types.MajorType(Types.MinorType.FLOAT8, Types.DataMode.REQUIRED)), allocator));
            else if(clazz == String.class || clazz == VarCharVector.class)
                vectors.add(new VarCharVector(MaterializedField.create("column" + index++,
                        new Types.MajorType(Types.MinorType.VARCHAR, Types.DataMode.REQUIRED)), allocator));
            else
                throw new IllegalArgumentException(String.format("Unsupported vector type %s", clazz));

        return vectors;
    }

    public static List<Class<?>> inferSchema(AugmentedString evidence) {
        List<Class<?>> classes = Lists.newArrayList();
        List<AugmentedString> columns = Lists.newArrayList();
        AugmentedString currentColumn = AugmentedString.empty;

        for(Object v: evidence.getState())
            if(v.equals(',') || v.equals(",")) {
                columns.add(currentColumn);
                currentColumn = AugmentedString.empty;
            } else if(v.equals('\n') || v.equals("\n")) {
                columns.add(currentColumn);
                break;
            } else
                currentColumn = AugmentedString.concat(currentColumn, v);

        for(AugmentedString s: columns) {
            if(tryParseLong(s))
                classes.add(Long.class);
            else if(tryParseDouble(s))
                classes.add(Double.class);
            else
                classes.add(String.class);
        }

        return classes;
    }

    private static boolean tryParseLong(String value) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean tryParseDouble(String value) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
