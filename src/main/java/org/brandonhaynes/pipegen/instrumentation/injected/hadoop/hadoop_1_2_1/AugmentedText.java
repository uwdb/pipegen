package org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_1_2_1;

import org.apache.hadoop.io.Text;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class AugmentedText extends Text {
    private Object[] state;
    private String decoratedString;

    public AugmentedText() {
        super();
    }

    public AugmentedText(String string) {
        super();
    }

    public AugmentedText(Text utf8) {
        super();
    }

    public AugmentedText(byte[] utf8) {
        super();
    }

    public AugmentedText(AugmentedText text) {
        super();
        this.state = text.state;
    }

    private String collapse() {
        return decoratedString == null
                ? (decoratedString = (String)(Object)new AugmentedString(state))
                : decoratedString;
    }

    @Override
    public byte[] getBytes() {
        return collapse().getBytes();
    }

    @Override
    public int getLength() {
        return collapse().length();
    }

    @Override
    public int charAt(int position) {
        return collapse().charAt(position);
    }

    @Override
    public int find(String what, int start) {
        return collapse().indexOf(what, start);
    }

    @Override
    public void set(Text other) {
        if (other instanceof AugmentedText)
            state = ((AugmentedText)other).state;
        else
            decoratedString = other.toString();
    }

    @Override
    public void set(byte[] utf8, int start, int len) {
        decoratedString = new String(utf8, start, len);
    }

    @Override
    public void append(byte[] utf8, int start, int len) {
        decoratedString = collapse() + new String(utf8, start, len);
    }

    @Override
    public void clear() {
        state = new Object[0];
        decoratedString = null;
    }

    @Override
    public String toString() {
        return collapse();
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        throw new UnsupportedOperationException("AugmentedText does not support this method.");
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.write(getBytes());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AugmentedText
                ? Arrays.equals(state, ((AugmentedText) o).state)
                : o.toString().equals(collapse());
    }

    @Override
    public int hashCode() {
        return Arrays.stream(state).mapToInt(Object::hashCode).reduce(0, (a, c) -> (a << 2) ^ c);
    }
}