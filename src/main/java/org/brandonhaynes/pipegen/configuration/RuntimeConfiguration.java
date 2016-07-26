package org.brandonhaynes.pipegen.configuration;

import org.apache.arrow.vector.BaseValueVector;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class RuntimeConfiguration {
    public static final String IMPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_IMPORT_VERIFICATION";
    public static final String EXPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_EXPORT_VERIFICATION";
    public static final String OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_OPTIMIZATION_MODE";
    public static Path configurationFile = Paths.get("/etc/pipegen/pipegen.yaml");
    private static RuntimeConfiguration instance;

    private final Pattern importFilenamePattern;
    private final Pattern exportFilenamePattern;
    private final URI workerDirectoryUri;

    public static RuntimeConfiguration getInstance() {
        if(instance == null) instance = new RuntimeConfiguration(configurationFile);
        return instance;
    }

    private RuntimeConfiguration(Path configurationFile) {
        // TODO
        this.importFilenamePattern = Pattern.compile(isInVerificationMode(Direction.IMPORT) ? "(?<name>.+)" : "__dbms__(?<name>.+)");
        this.exportFilenamePattern = Pattern.compile(isInVerificationMode(Direction.EXPORT) ? "(?<name>.+)" : "__dbms__(?<name>.+)");
        this.workerDirectoryUri = URI.create("http://localhost:8888");
    }

    public static void setProcessVerificationMode(ProcessBuilder builder,
                                                  boolean verificationMode, Direction direction) {
        setEnvironmentVariable(builder, direction == Direction.EXPORT
                                            ? EXPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME
                                            : IMPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME,
                               verificationMode);
    }
    public static void setProcessOptimizationMode(ProcessBuilder builder, boolean optimizationMode) {
        setEnvironmentVariable(builder, OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME, optimizationMode);
    }

    public boolean isOptimized() { return isInOptimizationMode(); }
    public int getVarCharSize() { return 1024; }
    public int getVectorSize() { return 4096; }
    public int getBufferAllocationSize() { return BaseValueVector.INITIAL_VALUE_ALLOCATION * 1024; }
    public Pattern getFilenamePattern(Direction direction) {
        return direction == Direction.EXPORT ? exportFilenamePattern : importFilenamePattern;
    }
    public URI getWorkerDirectoryUri() { return workerDirectoryUri; }
    public boolean isInVerificationMode(Direction direction) {
        return getEnvironmentFlag(direction == Direction.EXPORT
                ? EXPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME
                : IMPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME);
    }
    public boolean isInOptimizationMode() { return getEnvironmentFlag(OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME); }
    public long getIoTimeout() { return 50; }

    private static boolean getEnvironmentFlag(String variableName) {
        String value = System.getenv(variableName);
        return value != null && value.equals("true");
    }

    private static void setEnvironmentVariable(ProcessBuilder builder, String variable, boolean value) {
        builder.environment().put(variable, Boolean.toString(value));
    }
}
