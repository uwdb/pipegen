package org.brandonhaynes.pipegen.configuration;

import com.google.common.collect.Lists;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.brandonhaynes.pipegen.utilities.YamlUtilities.getElement;
import static org.brandonhaynes.pipegen.utilities.YamlUtilities.makeAbsolutePath;

public class DataPipeConfiguration {
    private static final boolean DEFAULT_DEBUG = false;

    private final Script verifyScript;
    private final Script importScript;
    private final Script exportScript;
    private final Path logPropertiesPath;
    private final boolean debug;

    DataPipeConfiguration(CompileTimeConfiguration configuration, Map yaml) {
        this(getScript(configuration, yaml.get("verify")),
             getScript(configuration, yaml.get("import")),
             getScript(configuration, yaml.get("export")),
             makeAbsolutePath(configuration, yaml.get("logProperties")),
             Boolean.parseBoolean(getElement(yaml, "debug", DEFAULT_DEBUG).toString()));
    }

    private DataPipeConfiguration(Script verifyScript,
                                  Script importScript, Script exportScript,
                                  Path logPropertiesPath, boolean debug) {
        this.verifyScript = verifyScript;
        this.importScript = importScript;
        this.exportScript = exportScript;
        this.logPropertiesPath = logPropertiesPath;
        this.debug = debug;
    }

    public Script getVerifyScript() {
        return verifyScript;
    }

    public Path getLogPropertiesPath() {
        return logPropertiesPath;
    }

    public boolean isDebug() {
        return debug;
    }

    public Script getImportScript() {
        return importScript;
    }

    public Script getExportScript() {
        return exportScript;
    }

    private static Script getScript(CompileTimeConfiguration configuration, Object value) {
        return new Script(configuration, value instanceof String
                ? Lists.newArrayList(value.toString())
                : ((List<?>)value).stream().map(Object::toString).collect(Collectors.toList()));
    }
}
