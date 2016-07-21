package org.brandonhaynes.pipegen.mutation.rules.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileInputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedInputStreamReader;
import org.brandonhaynes.pipegen.mutation.rules.ExpressionModificationRule;

import java.io.InputStreamReader;

class InputStreamReaderRule extends ExpressionModificationRule {
    InputStreamReaderRule(OptimizationTask task) {
        super(task, InputStreamReader.class, InterceptedInputStreamReader.class);
    }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals(sourceClass.getName()) &&
               !new StackFrame(node.get("stack").get(1).asText()).getMethodName().equals("intercept") &&
               node.get("arguments").size() > 0 &&
               node.get("arguments").get(0).asText()
                       .startsWith(InterceptedFileInputStream.class.getName());
    }
}
