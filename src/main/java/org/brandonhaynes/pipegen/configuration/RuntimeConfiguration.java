package org.brandonhaynes.pipegen.configuration;

import org.apache.arrow.vector.BaseValueVector;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class RuntimeConfiguration {
    public static final String VERIFICATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_VERIFICATION_MODE";
    public static final String OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_OPTIMIZATION_MODE";
    public static Path configurationFile = Paths.get("/etc/pipegen/pipegen.yaml");
    private static RuntimeConfiguration instance;

    private final Pattern filenamePattern;
    private final URI workerDirectoryUri;

    public static RuntimeConfiguration getInstance() {
        if(instance == null) instance = new RuntimeConfiguration(configurationFile);
        return instance;
    }

    private RuntimeConfiguration(Path configurationFile) {
        // TODO
        this.filenamePattern = Pattern.compile(isInVerificationMode() ? "(?<name>.+)" : "__dbms__(?<name>.+)");
        this.workerDirectoryUri = URI.create("http://localhost:8888");
    }

    public static void setProcessVerificationMode(ProcessBuilder builder, boolean verificationMode) {
        setPEnvironmentVariable(builder, VERIFICATION_ENVIRONMENT_VARIABLE_NAME, verificationMode);
    }
    public static void setProcessOptimizationMode(ProcessBuilder builder, boolean optimizationMode) {
        setPEnvironmentVariable(builder, OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME, optimizationMode);
    }

    public boolean isOptimized() { return isInOptimizationMode(); }
    public int getVarCharSize() { return 1024; }
    public int getVectorSize() { return 4096; }
    public int getBufferAllocationSize() { return BaseValueVector.INITIAL_VALUE_ALLOCATION * 1024; }
    public Pattern getFilenamePattern() { return filenamePattern; }
    public URI getWorkerDirectoryUri() { return workerDirectoryUri; }
    public boolean isInVerificationMode() { return getEnvironmentFlag(VERIFICATION_ENVIRONMENT_VARIABLE_NAME); }
    public boolean isInOptimizationMode() { return getEnvironmentFlag(OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME); }

    private static boolean getEnvironmentFlag(String variableName) {
        String value = System.getenv(variableName);
        return value != null && value.equals("true");
    }

    private static void setPEnvironmentVariable(ProcessBuilder builder, String variable, boolean value) {
        builder.environment().put(variable, Boolean.toString(value));
    }
}
