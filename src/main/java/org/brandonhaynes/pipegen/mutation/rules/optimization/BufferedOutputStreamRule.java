package org.brandonhaynes.pipegen.mutation.rules.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;
import org.brandonhaynes.pipegen.mutation.rules.ExpressionModificationRule;

import java.io.BufferedOutputStream;

class BufferedOutputStreamRule extends ExpressionModificationRule {
    BufferedOutputStreamRule(OptimizationTask task) { super(task, BufferedOutputStream.class,
                                                                  InterceptedBufferedOutputStream.class); }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").asText().equals(sourceClass.getName()) &&
               node.get("arguments").size() > 0 &&
               node.get("arguments").get(0).asText()
                        .startsWith(InterceptedFileOutputStream.class.getName());
    }
}
