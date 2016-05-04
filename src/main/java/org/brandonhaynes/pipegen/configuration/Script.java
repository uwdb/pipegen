package org.brandonhaynes.pipegen.configuration;

import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.List;

public class Script {
    private final List<String> commands;
    private final CompileTimeConfiguration configuration;

    public Script(CompileTimeConfiguration configuration, List<String> commands) {
        this.configuration = configuration;
        this.commands = Collections.unmodifiableList(commands);
    }

    public List<String> getCommands() { return commands; }
    public CompileTimeConfiguration getConfiguration() { return configuration; }

    public ProcessBuilder getProcessBuilder() {
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", this.toString());
        builder.directory(configuration.getBasePath().toFile());

        if(configuration.datapipeConfiguration.isDebug()) {
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        return builder;
    }

    @Override
    public String toString() {
        return Joiner.on(" && ").join(commands);
    }
}
