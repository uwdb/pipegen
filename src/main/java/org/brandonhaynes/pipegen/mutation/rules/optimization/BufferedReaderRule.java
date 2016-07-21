package org.brandonhaynes.pipegen.mutation.rules.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedReader;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedInputStreamReader;
import org.brandonhaynes.pipegen.mutation.rules.ExpressionModificationRule;

import java.io.BufferedReader;

class BufferedReaderRule extends ExpressionModificationRule {
    BufferedReaderRule(OptimizationTask task) { super(task, BufferedReader.class, InterceptedBufferedReader.class); }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals(sourceClass.getName()) &&
               node.get("arguments").size() > 0 &&
               node.get("arguments").get(0).asText()
                        .startsWith(InterceptedInputStreamReader.class.getName());
    }
}
