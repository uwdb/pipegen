package org.brandonhaynes.pipegen.configuration;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.google.common.io.Files;
import org.brandonhaynes.pipegen.configuration.tasks.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.brandonhaynes.pipegen.utilities.YamlUtilities.getChild;
import static org.brandonhaynes.pipegen.utilities.YamlUtilities.getElement;

public class CompileTimeConfiguration {
    private final String name;
    private final Path configurationFile;
    private final Version version;
    private final Path basePath;
    private final Path backupPath;

    public final InstrumentationConfiguration instrumentationConfiguration;
    public final OptimizationConfiguration optimizationConfiguration;
    public final DataPipeConfiguration datapipeConfiguration;
    public final ImportTask importTask;
    public final ExportTask exportTask;
    public final ExportOptimizationTask exportOptimizationTask;

    public CompileTimeConfiguration(String filename) throws IOException {
        this(Paths.get(filename));
    }

    public CompileTimeConfiguration(Path file) throws IOException {
        configurationFile = Paths.get(System.getProperty("user.dir")).resolve(file);
        Map yaml = getElement(new Yaml().load(Files.toString(configurationFile.toFile(), Charset.forName("UTF8"))), Map.class);

        name = yaml.get("name").toString();
        version = new Version(Integer.parseInt(yaml.get("version").toString()), 0);
        basePath = Paths.get(yaml.get("path").toString());
        backupPath = Paths.get(yaml.get("backupPath").toString());

        instrumentationConfiguration = new InstrumentationConfiguration(this, getChild(yaml, "instrumentation", Map.class));
        optimizationConfiguration = new OptimizationConfiguration(this, getChild(yaml, "optimization", Map.class));
        datapipeConfiguration = new DataPipeConfiguration(this, getChild(yaml, "datapipe", Map.class));
        importTask = new ImportTaskImpl(this);
        exportTask = new ExportTaskImpl(this);
        exportOptimizationTask = new ExportOptimizationTask(this);
    }

    public String getSystemName() { return name; }
    public Version getVersion() { return version; }
    public Path getBackupPath() { return backupPath; }
    public Path getBasePath() { return basePath; }
    public Path getConfigurationFile() { return configurationFile; }

}
