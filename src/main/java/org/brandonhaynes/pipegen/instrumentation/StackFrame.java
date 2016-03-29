package org.brandonhaynes.pipegen.instrumentation;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackFrame {
    private static final Pattern pattern =
            Pattern.compile("(?<class>[\\w\\.\\$]+)\\.(?<method>([\\w\\$]+|<init>))\\((?<file>\\w+\\.(java|scala))(:(?<line>\\d+)\\))?");

    private String stackFrame;
    private String className;
    private String methodName;
    private String fileName;
    private Optional<Integer> line;
    private boolean isParsed;

    public StackFrame(String stackFrame) {
        this.stackFrame = stackFrame;
    }

    private void parseStackFrame() {
        if(!isParsed && stackFrame != null) {
            Matcher matcher = pattern.matcher(stackFrame);
            if(!matcher.find())
                throw new RuntimeException("Failed to match stack frame " + stackFrame);

            className = matcher.group("class");
            methodName = matcher.group("method");
            fileName = matcher.group("file");
            line = matcher.group("line") != null
                    ? Optional.of(Integer.parseInt(matcher.group("line")))
                    : Optional.empty();
            isParsed = true;
        }
    }

    public String getStackFrame() { return stackFrame; }

    public String getClassName() {
        parseStackFrame();
        return className;
    }
    public String getMethodName() {
        parseStackFrame();
        return methodName;
    }
    public String getFileName() {
        parseStackFrame();
        return fileName;
    }
    public Optional<Integer> getLine() {
        parseStackFrame();
        return line;
    }

    @Override
    public int hashCode() {
        return stackFrame != null ? stackFrame.hashCode() : 0;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StackFrame &&
                ((stackFrame == null && ((StackFrame)other).getStackFrame() == null) ||
                 (stackFrame.equals(((StackFrame)other).getStackFrame())));
    }
}
