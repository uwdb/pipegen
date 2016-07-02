package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import io.netty.buffer.ArrowBuf;
import org.apache.arrow.memory.OutOfMemoryException;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.holders.Float8Holder;
import org.apache.arrow.vector.holders.IntHolder;
import org.apache.arrow.vector.holders.ValueHolder;
import org.apache.arrow.vector.holders.VarCharHolder;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeVector {
    private static final ArrowBuf[] emptyArrowBufArray = new ArrowBuf[0];

    private final List<ValueVector> vectors;
    private final Mutator mutator;
    private final Accessor accessor;
    private int index = 0, column = 0;

    public List<ValueVector> getVectors() { return vectors; }
    public Mutator getMutator() { return mutator; }
    public Accessor getAccessor() { return accessor; }

    public CompositeVector(ValueVector... vectors) {
        this(Lists.newArrayList(vectors));
    }

    CompositeVector(List<ValueVector> vectors) {
        if(vectors.size() == 0)
            throw new IllegalArgumentException("Composite must contain at least one vector.");

        this.vectors = vectors;
        this.accessor = new Accessor();
        this.mutator = new Mutator();
    }

    public void allocateNew() throws OutOfMemoryException {
        vectors.stream().forEach(ValueVector::allocateNew);
    }

    public void allocateNew(int totalBytes, int valueCount) {
        vectors.stream().forEach(v -> {
            if(v instanceof FixedWidthVector)
                ((FixedWidthVector) v).allocateNew(valueCount);
            else if(v instanceof VariableWidthVector)
                ((VariableWidthVector) v).allocateNew(totalBytes, valueCount);
        });
    }

    public ArrowBuf[] getBuffers(boolean clear) {
        return vectors.stream()
                      .map(CompositeVector::ensureVarCharWriterIndex)
                      .flatMap(v -> Arrays.stream(v.getBuffers(clear)))
                      .collect(Collectors.toList())
                      .toArray(emptyArrowBufArray);
    }

    public List<Class<?>> getClasses() {
        return vectors.stream().map(ValueVector::getClass).collect(Collectors.toList());
    }

    /**
     * Set the writer index for varchar vectors, which don't seem to expose a write lengths when calling getBuffers()
     */
    private static ValueVector ensureVarCharWriterIndex(ValueVector vector) {
        if(vector instanceof VarCharVector)
           vector.getBuffers(false)[1].writerIndex(((VarCharVector)vector).getVarByteLength());
        return vector;
    }

    public class Mutator implements ValueVector.Mutator {
        private final List<ValueVector.Mutator> mutators = vectors.stream().map(ValueVector::getMutator)
                                                                           .collect(Collectors.toList());

        @Override
        public void setValueCount(int i) {
            mutators.stream().forEach(m -> m.setValueCount(i));
        }

        @Override
        public void reset() {
            mutators.stream().forEach(ValueVector.Mutator::reset);
        }

        @Override
        @Deprecated
        @SuppressWarnings("deprecation")
        public void generateTestData(int i) {
            mutators.stream().forEach(m -> m.generateTestData(i));
        }

        public void set(AugmentedString value) {
            for(int i = 0; i < value.getState().length; i++)
                if(value.getState()[i] instanceof AugmentedString)
                    set((AugmentedString)value.getState()[i]);
                else if(value.isLong(i))
                    set(value.getLong(i));
                else if(value.isInteger(i))
                    set(value.getInteger(i));
                else if(value.isByte(i))
                    set(value.getByte(i));
                else if(value.isFloat(i))
                    set(value.getFloat(i));
                else if(value.isDouble(i))
                    set(value.getDouble(i));
                else if(!value.isDelimiter(i) && !value.isNewline(i))
                    set(value.getState()[i].toString());
        }

        public void set(int index, int value) {
            ((IntVector.Mutator)mutators.get(nextColumn())).set(index, value);
            mutators.get(column).setValueCount(index + 1);
        }

        public void set(int index, double value) {
            ((Float8Vector.Mutator)mutators.get(nextColumn())).set(index, value);
            mutators.get(column).setValueCount(index + 1);
        }

        public void set(int index, String value) {
            ((VarCharVector.Mutator)mutators.get(nextColumn())).setSafe(index, value.getBytes());
            mutators.get(column).setValueCount(index + 1);
        }

        public void set(int value) {
            set(checkIndex(), value);
        }

        public void set(double value) {
            set(checkIndex(), value);
        }

        public void set(String value) {
            set(checkIndex(), value);
        }

        private int checkIndex() { return column == vectors.size() - 1 ? index++ : index; }
        private int nextColumn() {
            int currentColumn = column;
            column = (column + 1) % vectors.size();
            return currentColumn;
        }
    }

    public class Accessor extends BaseValueVector.BaseAccessor {
        private final List<ValueVector.Accessor> accessors = vectors.stream().map(ValueVector::getAccessor)
                                                                             .collect(Collectors.toList());

        @Override
        public int getValueCount() {
            return accessors.stream().mapToInt(ValueVector.Accessor::getValueCount).max().orElse(0);
        }

        @Override
        public boolean isNull(int index) {
            return accessors.stream().allMatch(a -> a.isNull(index));
        }

        @Override
        public List<Object> getObject(int index) {
            return accessors.stream().map(a -> a.getObject(index)).collect(Collectors.toList());
        }

        public Object get(int column, int index) {
            return accessors.get(column).getObject(index);
        }

        public void get(int column, int index, ValueHolder holder) {
            ValueVector vector = vectors.get(0);
            if(vector instanceof IntVector)
                ((IntVector.Accessor)accessors.get(column)).get(index, (IntHolder)holder);
            else if(vector instanceof Float8Vector)
                ((Float8Vector.Accessor)accessors.get(column)).get(index, (Float8Holder)holder);
            else if(vector instanceof VarCharVector)
                ((VarCharVector.Accessor)accessors.get(column)).get(index, (VarCharHolder)holder);
        }

        public void get(int index, List<ValueHolder> holders) {
            int column = 0;
            for(ValueHolder holder: holders)
                get(index, column++, holder);
        }
    }
}
