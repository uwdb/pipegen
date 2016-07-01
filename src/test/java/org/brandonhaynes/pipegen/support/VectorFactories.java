package org.brandonhaynes.pipegen.support;

import io.netty.buffer.ArrowBuf;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.types.MaterializedField;
import org.apache.arrow.vector.types.Types;

public class VectorFactories {
    private static final BufferAllocator allocator = new RootAllocator(1024 * 1024);

    public static ArrowBuf createBuffer(int size) {
        return allocator.buffer(size);
    }

    public static IntVector createIntegerVector() {
        return new IntVector(MaterializedField.create("column",
                new Types.MajorType(Types.MinorType.INT, Types.DataMode.REQUIRED)), allocator);
    }

    public static Float8Vector createFloatVector() {
        return new Float8Vector(MaterializedField.create("column",
                new Types.MajorType(Types.MinorType.FLOAT8, Types.DataMode.REQUIRED)), allocator);
    }

    public static VarCharVector createStringVector() {
        return new VarCharVector(MaterializedField.create("column",
                new Types.MajorType(Types.MinorType.VARCHAR, Types.DataMode.REQUIRED)), allocator);
    }
}
