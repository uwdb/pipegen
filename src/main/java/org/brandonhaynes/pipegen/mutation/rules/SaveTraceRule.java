package org.brandonhaynes.pipegen.mutation.rules;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.File;
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
            for(int index = 0; index < trace.getRoot().size(); index++)
                writer.write(trace.getRoot().get(index).toString() + (index < trace.getRoot().size() - 1 ? ",\n" : "\n"));
            writer.write("]");
           //writer.write(new ObjectMapper().defaultPrettyPrintingWriter().defaultPrettyPrintingWriter().writeValueAsString(trace.getRoot().toString()));
            //writer.write(trace.getRoot().toString());
            return false;
        }
    }

    private Path getFile() {
        return Paths.get(String.format("%s/%s-%s.json",
                configuration.instrumentationConfiguration.getLogPath(), configuration.getSystemName().toLowerCase(), getTimestamp())); }
    private static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
    }
}
