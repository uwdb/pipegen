package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class SaveTraceRule implements Rule {
    private static final Logger log = Logger.getLogger(SaveTraceRule.class.getName());
    private final CompileTimeConfiguration configuration;

    public SaveTraceRule(CompileTimeConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isApplicable(TraceResult trace) {
        return true;
    }

    @Override
    public boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException {
        Path traceFilename = getFile();
        log.info("Saving trace to " + traceFilename);

        try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(traceFilename.toFile()))) {
            writer.write("[");
            for(JsonNode node: trace.getNodes())
                writer.write(node.toString() + ",\n");
            writer.write("null]");
            return false; // Don't count this rule as a modification
        }
    }

    private Path getFile() {
        return Paths.get(String.format("%s/%s-%s.json",
                configuration.instrumentationConfiguration.getLogPath(), configuration.getSystemName().toLowerCase(), getTimestamp())); }
    private static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
    }
}
