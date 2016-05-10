package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Logger;

public class ExpressionClassSubstitutionRule implements Rule {
    private static final Logger log = Logger.getLogger(ExpressionClassSubstitutionRule.class.getName());

    private String key;
    private String expression;
    private Class replacementClass;
    private File[] jars;

    public ExpressionClassSubstitutionRule(String key, String expression, Class replacementClass, Path jarDirectory) {
        this(key, expression, replacementClass, jarDirectory.toFile().listFiles((dir, name) -> name.endsWith("jar")));
    }

    public ExpressionClassSubstitutionRule(String key, String expression, Class replacementClass, File[] jars) {
        this.key = key;
        this.expression = expression;
        this.replacementClass = replacementClass;
        this.jars = jars;
    }

    public boolean isApplicable(TraceResult trace) {
        return !getNodes(trace).isEmpty();
    }

    public boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException {
        for(JsonNode entry: getNodes(trace)) {
            //if(entry.get("state").asText() != null && !entry.get("state").asText().isEmpty())
            if(!entry.get("state").get("path").asText().isEmpty() && !entry.get("state").get("path").asText().equals("null"))
                log.info(entry.get("state").get("path").asText());
            //InheritanceAugmenter.interceptInherited(entry.findValue("class").asText(), replacementClass, jars);
        }
        return isApplicable(trace);
    }

    private Collection<JsonNode> getNodes(TraceResult trace) {
        JsonNode values;
        Collection<JsonNode> nodes = Lists.newArrayList();

        for(JsonNode entry: trace.getRoot())
            if((values = entry.findValue(key)) != null)
                for(JsonNode value: values)
                    if(value.asText().matches(expression))
                        nodes.add(entry);
        return nodes;
    }
}
