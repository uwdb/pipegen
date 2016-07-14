package org.brandonhaynes.pipegen.mutation.rules.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;
import org.brandonhaynes.pipegen.mutation.rules.ModificationRule;

import java.io.IOException;

class InterceptedFileOutputStreamRule extends ModificationRule {
    InterceptedFileOutputStreamRule(OptimizationTask task) {
        super(task, InterceptedFileOutputStream.class);
    }

    @Override
    protected boolean modifyCallSite(JsonNode node, StackFrame frame)
            throws IOException, NotFoundException, CannotCompileException {
        return true;
    }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals("java.net.SocketOutputStream") &&
               node.get("stack").get(1).asText().startsWith(sourceClass.getName());
    }
}
