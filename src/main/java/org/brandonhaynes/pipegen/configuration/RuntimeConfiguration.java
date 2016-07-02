package org.brandonhaynes.pipegen.configuration;

import org.apache.arrow.vector.BaseValueVector;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class RuntimeConfiguration {
    public static final String verificationEnvironmentVariableName = "PIPEGEN_RUNTIME_VERIFICATION_MODE";
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
        builder.environment().put(verificationEnvironmentVariableName, Boolean.toString(verificationMode));
    }

    public boolean isOptimized() { return true; }
    public long getBufferAllocationSize() { return BaseValueVector.INITIAL_VALUE_ALLOCATION * 1024; }
    public Pattern getFilenamePattern() { return filenamePattern; }
    public URI getWorkerDirectoryUri() { return workerDirectoryUri; }
    public boolean isInVerificationMode() {
        String isInVerificationMode = System.getenv(verificationEnvironmentVariableName);
        return isInVerificationMode != null && isInVerificationMode.equals("true");
    }
}
