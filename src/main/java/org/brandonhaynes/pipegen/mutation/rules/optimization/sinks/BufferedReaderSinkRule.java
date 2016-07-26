package org.brandonhaynes.pipegen.mutation.rules.optimization.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedReader;
import org.brandonhaynes.pipegen.mutation.rules.ModificationRule;

import java.io.IOException;

public class BufferedReaderSinkRule extends ModificationRule {
    public BufferedReaderSinkRule(OptimizationTask task) {
        super(task, InterceptedBufferedReader.class);
    }

    @Override
    protected boolean modifyCallSite(JsonNode node, StackFrame frame)
            throws IOException, NotFoundException, CannotCompileException {
        task.getModifiedCallSites().add(getApplicationStackFrame(node));
        return false;
    }

    protected boolean isRelevantStackFrame(StackFrame frame) {
        return !frame.getClassName().equals(sourceClass.getName()) &&
                (frame.getMethodName().equals("read") ||
                 frame.getMethodName().equals("readLine"));
    }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals(sourceClass.getName());
    }

    private StackFrame getApplicationStackFrame(JsonNode node) {
        for(int i = 0; i < node.get("stack").size(); i++) {
            StackFrame frame = new StackFrame(node.get("stack").get(i).asText());
            if(!frame.getClassName().startsWith("java.") && !frame.getClassName().startsWith("sun."))
                return frame;
        }

        throw new RuntimeException("Expected a non-Java stack frame.");
    }
}
