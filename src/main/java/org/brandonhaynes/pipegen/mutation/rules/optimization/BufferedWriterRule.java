package org.brandonhaynes.pipegen.mutation.rules.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedWriter;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedOutputStreamWriter;
import org.brandonhaynes.pipegen.mutation.rules.ExpressionModificationRule;

import java.io.BufferedWriter;

class BufferedWriterRule extends ExpressionModificationRule {
    BufferedWriterRule(OptimizationTask task) {
        super(task, BufferedWriter.class, InterceptedBufferedWriter.class);
    }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals(sourceClass.getName()) &&
               node.get("state").get("out").asText()
                        .startsWith(InterceptedOutputStreamWriter.class.getName());
    }
}
