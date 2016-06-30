package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.types.MaterializedField;
import org.apache.arrow.vector.types.Types;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import java.lang.reflect.Type;
import java.util.List;

public class ColumnUtilities {
    public static List<ValueVector> createVectors(BufferAllocator allocator, AugmentedString evidence) {
        return createVectors(allocator, inferSchema(evidence));
    }

    public static List<ValueVector> createVectors(BufferAllocator allocator, List<Type> types) {
        int index = 0;
        List<ValueVector> vectors = Lists.newArrayList();

        for(Type type: types)
            if(type == Long.class)
                vectors.add(new IntVector(MaterializedField.create("column" + index++,
                        new Types.MajorType(Types.MinorType.INT, Types.DataMode.REQUIRED)), allocator));
            else if(type == Double.class)
                vectors.add(new Float8Vector(MaterializedField.create("column" + index++,
                        new Types.MajorType(Types.MinorType.FLOAT8, Types.DataMode.REQUIRED)), allocator));
            else if(type == String.class)
                vectors.add(new VarCharVector(MaterializedField.create("column" + index++,
                        new Types.MajorType(Types.MinorType.VARCHAR, Types.DataMode.REQUIRED)), allocator));
            else
                throw new IllegalArgumentException(String.format("Unsupported vector type %s", type));

        return vectors;
    }

    public static List<Type> inferSchema(AugmentedString evidence) {
        List<Type> types = Lists.newArrayList();
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
                types.add(Long.class);
            else if(tryParseDouble(s))
                types.add(Double.class);
            else
                types.add(String.class);
        }

        return types;
    }

    private static boolean tryParseLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean tryParseDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
