package org.brandonhaynes.pipegen.configuration;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.commons.io.FileUtils;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.mutation.rules.ExportRule;
import org.brandonhaynes.pipegen.mutation.rules.ImportRule;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.runtime.proxy.ExportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.ImportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CompileTimeConfiguration {
    private final String name;
    private final Path configurationFile;
    private final Version version;
    private final Path basePath;
    private final Collection<Path> classPaths;
    private final Collection<Path> excludeClassPaths;
    private final Collection<ClassPath> classPoolPaths = Lists.newArrayList();
    private final ClassPool pool;

    public final InstrumentationConfiguration instrumentationConfiguration;
    public final DataPipeConfiguration datapipeConfiguration;
    public final ImportTask importTask;
    public final ExportTask exportTask;

    public CompileTimeConfiguration(String filename) throws IOException {
        this(Paths.get(filename));
    }

    public CompileTimeConfiguration(Path file) throws IOException {
        configurationFile = Paths.get(System.getProperty("user.dir")).resolve(file);
        Map yaml = getElement(new Yaml().load(Files.toString(configurationFile.toFile(), Charset.forName("UTF8"))), Map.class);

        name = yaml.get("name").toString();
        version = new Version(Integer.parseInt(yaml.get("version").toString()), 0);
        basePath = Paths.get(yaml.get("path").toString());
        classPaths = getClassPaths(getChild(yaml, "classPaths", List.class));
        excludeClassPaths = getClassPaths(getChild(getChild(yaml, "optimization", Map.class), "excludeClassPaths", List.class));

        instrumentationConfiguration = new InstrumentationConfiguration(getChild(yaml, "instrumentation", Map.class));
        datapipeConfiguration = new DataPipeConfiguration(getChild(yaml, "datapipe", Map.class));
        importTask = new ImportTask(this);
        exportTask = new ExportTask(this);

        try {
            pool = new ClassPool(false); // ClassPool.getDefault();
            pool.appendSystemPath();
            for(Path path: classPaths) {
                classPoolPaths.add(pool.insertClassPath(path.toString()));
            }
        } catch(NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSystemName() { return name; }
    public Version getVersion() { return version; }
    public Path getBasePath() { return basePath; }
    public Path getBackupPath() { return Paths.get("/tmp/backup"); } //TODO

    public ClassPool getClassPool() {
        return pool;
    }
    public Collection<Path> getClassPaths() {
        return classPaths;
    }
    public Collection<Path> getExcludeClassPaths() {
        return excludeClassPaths;
    }

    public Iterable<URL> findClasses(String className) {
        return classPoolPaths.stream()
                         .map(path -> path.find(className))
                         .filter(url -> url != null)
                         .collect(Collectors.toList());
    }

    private static <T> T getElement(Object yaml, Class<T> clazz) throws IOException {
        try {
            return clazz.cast(yaml);
        } catch(ClassCastException e) {
            throw new IOException(String.format("Expected a %s as child element", clazz), e);
        }
    }

    private static <T> T getChild(Map yaml, String key, Class<T> clazz) throws IOException {
        if(!yaml.containsKey(key))
            throw new IOException("Configuration file does not contain property " + key);
        else
            return getElement(yaml.get(key), clazz);
    }

    private Collection<Path> getClassPaths(List yaml) {
        Collection<Path> paths = Lists.newArrayList();
        for(Object path: yaml)
            if(path.toString().startsWith("recurse:"))
                paths.addAll(getJarsRecursively(makeAbsolutePath(path.toString().substring("recurse:".length()).replace("*", ""))));
            else
                paths.add(Paths.get(makeAbsolutePath(path).toString()));
        return paths;
    }

    private Collection<Path> getJarsRecursively(Path path) {
        return Lists.newArrayList(FileUtils.iterateFiles(path.toFile(), new String[] {"jar"}, true))
                    .stream()
                    .map(f -> f.toPath())
                    .collect(Collectors.toList());
    }

    private Path makeAbsolutePath(Object path) {
        return path != null
                ? getBasePath().resolve(path.toString()
                    .replace("$CONFIG_DIR", configurationFile.getParent().toString())
                    .replace("BASE_DIR", getBasePath().toString())
                    .replace("$DIR", System.getProperty("user.dir")))
                : null;
    }

    public class InstrumentationConfiguration {
        private final int port;
        private final int timeout;
        private final Path traceFile;
        private final Path agentFile;
        private final Path logPath;
        private final Pattern commandPattern;
        private final Pattern classPattern;
        private final boolean debug;

        InstrumentationConfiguration(Map yaml) {
            this(Integer.parseInt(yaml.get("port").toString()),
                    Integer.parseInt(yaml.get("timeout").toString()),
                    makeAbsolutePath(yaml.get("trace")),
                    makeAbsolutePath(yaml.get("agent")),
                    makeAbsolutePath(yaml.get("logs")),
                    Pattern.compile(yaml.get("commands").toString()),
                    Pattern.compile(yaml.get("classes").toString()),
                    Boolean.parseBoolean(yaml.get("debug").toString()));
        }

        InstrumentationConfiguration(int port, int timeout, Path traceFile, Path agentFile, Path logPath,
                                     Pattern commandPattern, Pattern classPattern, boolean debug) {
            this.port = port;
            this.timeout = timeout;
            this.traceFile = traceFile;
            this.agentFile = agentFile;
            this.logPath = logPath;
            this.commandPattern = commandPattern;
            this.classPattern = classPattern;
            this.debug = debug;
        }

        public int getPort() { return port; }
        public int getTimeout() { return timeout; }
        public Path getTraceFile() { return traceFile; }
        public Path getAgentFile() { return agentFile; }
        public Path getLogPath() { return logPath; }
        public Pattern getClassPattern() { return classPattern; }
        public Pattern getCommandPattern() { return commandPattern; }
        public boolean isDebug() { return debug; }
    }

    public class DataPipeConfiguration {
        private final Script verifyScript;
        private final Script importScript;
        private final Script exportScript;
        private final Path logPropertiesPath;
        private final boolean debug;

        DataPipeConfiguration(Map yaml) {
            this(getScript(yaml.get("verify")),
                    getScript(yaml.get("import")),
                    getScript(yaml.get("export")),
                    makeAbsolutePath(yaml.get("logProperties")),
                    Boolean.parseBoolean(yaml.get("debug").toString()));
        }

        DataPipeConfiguration(Script verifyScript,
                              Script importScript, Script exportScript,
                              Path logPropertiesPath, boolean debug) {
            this.verifyScript = verifyScript;
            this.importScript = importScript;
            this.exportScript = exportScript;
            this.logPropertiesPath = logPropertiesPath;
            this.debug = debug;
        }

        public Script getVerifyScript() { return verifyScript; }
        public Path getLogPropertiesPath() { return logPropertiesPath; }
        public boolean isDebug() { return debug; }
        Script getImportScript() { return importScript; }
        Script getExportScript() { return exportScript; }
    }


    private static abstract class Task implements org.brandonhaynes.pipegen.configuration.Task {
        private final CompileTimeConfiguration configuration;
        private final Script script;
        private final Set<StackFrame> modifiedCallSites = Sets.newHashSet();

        private Task(CompileTimeConfiguration configuration, Script script) {
            this.configuration = configuration;
            this.script = script;
        }

        public CompileTimeConfiguration getConfiguration() { return configuration; }
        public Script getTaskScript() { return script; }
        public Set<StackFrame> getModifiedCallSites() { return modifiedCallSites; }
    }

    private static class ImportTask extends Task implements org.brandonhaynes.pipegen.configuration.ImportTask {
        private ImportVerificationProxy proxy;
        private Rule rule;

        private ImportTask(CompileTimeConfiguration configuration) {
            super(configuration, configuration.datapipeConfiguration.getImportScript());
            proxy = new ImportVerificationProxy(configuration.getBasePath());
            rule = new ImportRule(this);
        }
        public Script getImportScript() { return getTaskScript(); }
        public VerificationProxy getVerificationProxy() { return proxy; }
        public Rule getRule() { return rule; }
        }

    private static class ExportTask extends Task implements org.brandonhaynes.pipegen.configuration.ExportTask {
        private ExportVerificationProxy proxy;
        private Rule rule;

        private ExportTask(CompileTimeConfiguration configuration) throws IOException {
            super(configuration, configuration.datapipeConfiguration.getExportScript());
            proxy = new ExportVerificationProxy(configuration.getBasePath());
            rule = new ExportRule(this);
        }

        public Script getExportScript() { return getTaskScript(); }
        public VerificationProxy getVerificationProxy() { return proxy; }
        public Rule getRule() { return rule; }
    }

    private Script getScript(Object value) {
        return new Script(this, value instanceof String
            ? Lists.newArrayList(value.toString())
            : ((List<?>)value).stream().map(Object::toString).collect(Collectors.toList()));
    }
}
