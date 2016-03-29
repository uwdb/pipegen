package org.brandonhaynes.pipegen.instrumentation.injected.jackson;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Consumer;

public class RemappingJsonWriter extends Writer {
    private List<Consumer> actions = Lists.newArrayList();
    private Multimap<String, Object> values = ArrayListMultimap.create();

    private final Writer decoratedWriter;
    private State currentState = State.START;
    private enum State {
        START,
        IN_MAP
    }
    private enum Transition {
        START_OBJECT
    }

    public RemappingJsonWriter(Writer decoratedWriter) {
        this.decoratedWriter = decoratedWriter;
    }

    void add(Multimap<String, Object> values, List<Consumer> actions) {
        this.values.putAll(values);
        this.actions.addAll(actions);
    }

    @Override
    public void write(char[] chars, int start, int length) throws IOException {
        currentState = transition(currentState, getTransition());
        decoratedWriter.write(chars, start, length);
    }

    @Override
    public void flush() throws IOException {
        decoratedWriter.flush();
    }

    @Override
    public void close() throws IOException {
        decoratedWriter.close();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int rowCount = values.keySet().stream().map(k -> values.get(k).size()).findFirst().orElse(0);

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
        //return decoratedWriter.toString();
    }

    private State transition(State state, Transition transition) {
        switch(currentState) {
            case START:
                if(transition == Transition.START_OBJECT)
                    return State.IN_MAP;
            default:
                throw new IllegalStateException("Unsupported transition " + transition + " for state " + state);
        }
    }

    private static Transition getTransition() {
        return getTransition(new Throwable().getStackTrace());
    }

    private static Transition getTransition(StackTraceElement[] stackTrace) {
        for(StackTraceElement element: stackTrace)
            switch(element.getMethodName()) {
                case "writeStartObject":
                    return Transition.START_OBJECT;
            }

        throw new UnsupportedOperationException("Did not recognize a supported next state in stack " +
                Joiner.on("\n").join(stackTrace));
    }
}
