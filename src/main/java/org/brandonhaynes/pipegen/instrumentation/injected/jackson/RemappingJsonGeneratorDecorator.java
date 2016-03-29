package org.brandonhaynes.pipegen.instrumentation.injected.jackson;

import com.fasterxml.jackson.core.*;
import com.google.common.collect.*;
import io.netty.buffer.ArrowBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

public class RemappingJsonGeneratorDecorator extends JsonGenerator {
    private final JsonGenerator generator;
    private State currentState = State.START;
    private Object metadata;

    private enum State {
        START,
        IN_MAP
    }
    private enum Transition {
        START_OBJECT, END_OBJECT,
        FIELDNAME, VALUE,
        NONE
    }
    private List<Consumer> actions = Lists.newArrayList();
    private Multimap<String, Object> values = ArrayListMultimap.create();

    public RemappingJsonGeneratorDecorator(JsonGenerator generator) {
        this.generator = generator;

        if(!(generator.getOutputTarget() instanceof RemappingJsonWriter))
            throw new UnsupportedOperationException("Excpeted a RemappingJsonWriter output target.");
        /*
        Field field;
        try {
            field = generator.getClass().getDeclaredField("_writer");
            field.setAccessible(true);
            field.set(generator, new RemappingJsonWriter((Writer)field.get(generator)));
        } catch(NoSuchFieldException|IllegalAccessException e) {
            throw new UnsupportedOperationException("Decorated generator must contain _writer field", e);
        }
        */
    }

    private void transition(Transition transition, Consumer action) {
        actions.add(action);
        currentState = transition(currentState, transition, null);
    }

    private void transition(Transition transition, Object value, Consumer action) {
        actions.add(action);
        currentState = transition(currentState, transition, value);
    }

    private State transition(State state, Transition transition, Object value) {
        switch(currentState) {
            case START:
                if(transition == Transition.START_OBJECT)
                    return State.IN_MAP;
                break;
            case IN_MAP:
                if(transition == Transition.FIELDNAME) {
                    metadata = value;
                    return State.IN_MAP;
                } else if(transition == Transition.VALUE) {
                    values.put(metadata.toString(), value);
                    return State.IN_MAP;
                } else if(transition == Transition.END_OBJECT) {
                    ensureMissingValues();
                    return State.START;
                }
        }

        if(transition == Transition.NONE)
            return state;
        else
            throw new IllegalStateException("Unsupported transition " + transition + " for state " + state);
    }

    private void ensureMissingValues() {
        int rows = rowCount();
        for(String key: values.keySet())
            while(values.get(key).size() < rows)
                values.get(key).add(null);
    }

    private int rowCount() {
        return values.keySet().stream().map(k -> values.get(k).size()).max(Integer::max).orElse(0);
    }

    private RemappingJsonWriter getWriter() {
        return (RemappingJsonWriter)generator.getOutputTarget();
    }

    @Override
    public JsonGenerator setCodec(ObjectCodec objectCodec) {
        return generator.setCodec(objectCodec);
    }

    @Override
    public ObjectCodec getCodec() {
        return generator.getCodec();
    }

    @Override
    public Version version() {
        return generator.version();
    }

    @Override
    public JsonGenerator enable(Feature feature) {
        return generator.enable(feature);
    }

    @Override
    public JsonGenerator disable(Feature feature) {
        return generator.disable(feature);
    }

    @Override
    public boolean isEnabled(Feature feature) {
        return generator.isEnabled(feature);
    }

    @Override
    public int getFeatureMask() {
        return generator.getFeatureMask();
    }

    @Override
    @SuppressWarnings("deprecation")
    public JsonGenerator setFeatureMask(int i) {
        return generator.setFeatureMask(i);
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return generator.useDefaultPrettyPrinter();
    }

    @Override
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void writeStartObject() throws IOException {
        transition(Transition.START_OBJECT, (x) -> {
            try {
                generator.writeStartObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void writeEndObject() throws IOException {
        transition(Transition.END_OBJECT, (x) -> {
            try {
                generator.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void writeFieldName(String s) throws IOException {
        transition(Transition.FIELDNAME, s, (x) -> {
            try {
                generator.writeFieldName(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void writeFieldName(SerializableString serializableString) throws IOException {
        generator.writeFieldName(serializableString);
    }

    @Override
    public void writeString(String s) throws IOException {
        generator.writeString(s);
    }

    @Override
    public void writeString(char[] chars, int i, int i1) throws IOException {
        generator.writeString(chars, i, i1);
    }

    @Override
    public void writeString(SerializableString serializableString) throws IOException {
        generator.writeString(serializableString);
    }

    @Override
    public void writeRawUTF8String(byte[] bytes, int i, int i1) throws IOException {
        generator.writeRawUTF8String(bytes, i, i1);
    }

    @Override
    public void writeUTF8String(byte[] bytes, int i, int i1) throws IOException {
        generator.writeUTF8String(bytes, i, i1);
    }

    @Override
    public void writeRaw(String s) throws IOException {
        generator.writeRaw(s);
    }

    @Override
    public void writeRaw(String s, int i, int i1) throws IOException {
        generator.writeRaw(s, i, i1);
    }

    @Override
    public void writeRaw(char[] chars, int i, int i1) throws IOException {
        generator.writeRaw(chars, i, i1);
    }

    @Override
    public void writeRaw(char c) throws IOException {
        generator.writeRaw(c);
    }

    @Override
    public void writeRawValue(String s) throws IOException {
        generator.writeRawValue(s);
    }

    @Override
    public void writeRawValue(String s, int i, int i1) throws IOException {
        generator.writeRawValue(s, i, i1);
    }

    @Override
    public void writeRawValue(char[] chars, int i, int i1) throws IOException {
        generator.writeRawValue(chars, i, i1);
    }

    @Override
    public void writeBinary(Base64Variant base64Variant, byte[] bytes, int i, int i1) throws IOException {
        generator.writeBinary(base64Variant, bytes, i, i1);
    }

    @Override
    public int writeBinary(Base64Variant base64Variant, InputStream inputStream, int i) throws IOException {
        return generator.writeBinary(base64Variant, inputStream, i);
    }

    @Override
    public void writeNumber(int i) throws IOException {
        generator.writeNumber(i);
    }

    @Override
    public void writeNumber(long l) throws IOException {
        generator.writeNumber(l);
    }

    @Override
    public void writeNumber(BigInteger bigInteger) throws IOException {
        generator.writeNumber(bigInteger);
    }

    @Override
    public void writeNumber(double v) throws IOException {
        generator.writeNumber(v);
    }

    @Override
    public void writeNumber(float v) throws IOException {
        generator.writeNumber(v);
    }

    @Override
    public void writeNumber(BigDecimal bigDecimal) throws IOException {
        generator.writeNumber(bigDecimal);
    }

    @Override
    public void writeNumber(String s) throws IOException {
        generator.writeNumber(s);
    }

    @Override
    public void writeBoolean(boolean b) throws IOException {
        generator.writeBoolean(b);
    }

    @Override
    public void writeNull() throws IOException {
        generator.writeNull();
    }

    @Override
    public void writeObject(Object o) throws IOException {
        transition(Transition.VALUE, o, (x) -> {
            try {
                generator.writeObject(o);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void writeTree(TreeNode treeNode) throws IOException {
        generator.writeTree(treeNode);
    }

    @Override
    public JsonStreamContext getOutputContext() {
        return generator.getOutputContext();
    }

    @Override
    public void flush() throws IOException {
        getWriter().add(values, actions);
        values = ArrayListMultimap.create();
        actions = Lists.newArrayList();
    }

    @Override
    public boolean isClosed() {
        return generator.isClosed();
    }

    @Override
    public void close() throws IOException {
        generator.close();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int rowCount = rowCount();

        for(int i = 0; i < rowCount; i++) {
            builder.append('{');
            for(int k = 0; k < values.keySet().size(); k++) {
                String key = values.keySet().toArray()[k].toString();
                builder.append(key).append('=').append(values.get(key).toArray()[i]);
                if (k != values.keySet().size() - 1)
                    builder.append(',');
            }
            builder.append("}\n");
        }

        return builder.toString();
    }
}
