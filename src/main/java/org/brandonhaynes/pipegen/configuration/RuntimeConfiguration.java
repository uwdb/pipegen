package org.brandonhaynes.pipegen.configuration;

import org.apache.arrow.vector.BaseValueVector;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RuntimeConfiguration {
    private static final String DEFAULT_CONFIGURATION_FILENAME = "/etc/pipegen/pipegen.yaml";

    private static final String DEFAULT_IMPORT_PATTERN = "__dbms__(?<name>.+)";
    private static final String DEFAULT_VERIFICATION_IMPORT_PATTERN = "(?<name>.+)";
    private static final String DEFAULT_EXPORT_PATTERN = "__dbms__(?<name>.+)";
    private static final String DEFAULT_VERIFICATION_EXPORT_PATTERN = "(?<name>^(?!_SUCCESS).+$)";

    private static final String DEFAULT_DIRECTORY_URI = "http://localhost:8888";

    private static final String CONFIGURATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_CONFIG";
    private static final String IMPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_IMPORT_VERIFICATION";
    private static final String EXPORT_VERIFICATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_EXPORT_VERIFICATION";
    private static final String OPTIMIZATION_ENVIRONMENT_VARIABLE_NAME = "PIPEGEN_RUNTIME_OPTIMIZATION_MODE";

    private static RuntimeConfiguration instance;

    private final Pattern importFilenamePattern;
    private final Pattern exportFilenamePattern;
    private final URI workerDirectoryUri;

    public static RuntimeConfiguration getInstance() {
        if(instance == null) instance = new RuntimeConfiguration(getFilename());
        return instance;
    }

    public static Path getFilename() {
        String filename = System.getenv(CONFIGURATION_ENVIRONMENT_VARIABLE_NAME);
        return Paths.get(filename != null ? filename : DEFAULT_CONFIGURATION_FILENAME);
    }

    private RuntimeConfiguration(Path configurationFile) {
        Map<String, String> yaml = getYaml(configurationFile);

        this.importFilenamePattern = Pattern.compile(isInVerificationMode(Direction.IMPORT)
                ? yaml.getOrDefault("importVerificationPattern", DEFAULT_VERIFICATION_IMPORT_PATTERN)
                : yaml.getOrDefault("importPattern", DEFAULT_IMPORT_PATTERN));
        this.exportFilenamePattern = Pattern.compile(isInVerificationMode(Direction.EXPORT)
                ? yaml.getOrDefault("importVerificationPattern", DEFAULT_VERIFICATION_EXPORT_PATTERN)
                : yaml.getOrDefault("exportPattern", DEFAULT_EXPORT_PATTERN));
        this.workerDirectoryUri = URI.create(yaml.getOrDefault("workerDirectoryUri", DEFAULT_DIRECTORY_URI));
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

    private static Map<String, String> getYaml(Path configurationFile) {
        try {
            return configurationFile.toFile().exists()
                    ? convertToMap((readFileAsString(configurationFile)))
                    : new HashMap<>();
        } catch(IOException e) {
            throw new RuntimeException("Runtime configuration file exists but could not be read.", e);
        }
    }

    private static boolean getEnvironmentFlag(String variableName) {
        String value = System.getenv(variableName);
        return value != null && value.equals("true");
    }

    private static void setEnvironmentVariable(ProcessBuilder builder, String variable, boolean value) {
        builder.environment().put(variable, Boolean.toString(value));
    }

    private static String readFileAsString(Path file) throws IOException {
        return String.join("\n", Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    // Fake YAML parsing to avoid taking a dependency on another library that we'd have to inject
    private static Map<String, String> convertToMap(String value) {
        Map<String, String> map = new HashMap<>();
        Arrays.stream(value.split("\n")).map(String::trim)
                                        .map(line -> line.split(":"))
                                        .forEach(pair -> map.put(pair[0].trim(), pair[1].trim()));
        return map;
    }
}
