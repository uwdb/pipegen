package org.brandonhaynes.pipegen.mutation.rules.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedOutputStreamWriter;
import org.brandonhaynes.pipegen.mutation.rules.ExpressionModificationRule;

import java.io.OutputStreamWriter;

class OutputStreamWriterRule extends ExpressionModificationRule {
    OutputStreamWriterRule(OptimizationTask task) {
        super(task, OutputStreamWriter.class, InterceptedOutputStreamWriter.class);
    }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals(sourceClass.getName()) &&
               !new StackFrame(node.get("stack").get(1).asText()).getMethodName().equals("intercept") &&
               node.get("arguments").size() > 0 &&
               (node.get("arguments").get(0).asText()
                                            .startsWith(InterceptedFileOutputStream.class.getName()) ||
                node.get("arguments").get(0).asText()
                                            .startsWith(InterceptedBufferedOutputStream.class.getName()));
    }
}
