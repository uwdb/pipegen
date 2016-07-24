package org.brandonhaynes.pipegen.configuration;

import com.google.common.collect.Lists;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.utilities.YamlUtilities;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.brandonhaynes.pipegen.utilities.YamlUtilities.getChild;
import static org.brandonhaynes.pipegen.utilities.YamlUtilities.makeAbsolutePath;

public class InstrumentationConfiguration {
    private final Collection<Path> classPaths;
    private final Collection<ClassPath> classPoolPaths = Lists.newArrayList();
    private final ClassPool pool;
    private final int port;
    private final int timeout;
    private final Path traceFile;
    private final Path agentFile;
    private final Path logPath;
    private final Pattern commandPattern;
    private final Pattern classPattern;
    private final boolean debug;

    InstrumentationConfiguration(CompileTimeConfiguration configuration, Map yaml) throws IOException {
        this(Integer.parseInt(yaml.get("port").toString()),
             Integer.parseInt(yaml.get("timeout").toString()),
             makeAbsolutePath(configuration, yaml.get("trace")),
             makeAbsolutePath(configuration, yaml.get("agent")),
             makeAbsolutePath(configuration, yaml.get("logPath")),
             Pattern.compile(yaml.get("commands").toString()),
             Pattern.compile(yaml.get("classes").toString()),
             YamlUtilities.getClassPaths(configuration,
                                                  getChild(yaml, "classPaths", List.class)),
             Boolean.parseBoolean(yaml.get("debug").toString()));
    }

    private InstrumentationConfiguration(int port, int timeout, Path traceFile, Path agentFile, Path logPath,
                                         Pattern commandPattern, Pattern classPattern, Collection<Path> classPaths,
                                         boolean debug) {
        this.port = port;
        this.timeout = timeout;
        this.traceFile = traceFile;
        this.agentFile = agentFile;
        this.logPath = logPath;
        this.commandPattern = commandPattern;
        this.classPattern = classPattern;
        this.debug = debug;
        this.classPaths = classPaths;

        try {
            pool = new ClassPool(false); // ClassPool.getDefault();
            pool.appendSystemPath();
            for (Path path : classPaths) {
                classPoolPaths.add(pool.insertClassPath(path.toString()));
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public Path getTraceFile() {
        return traceFile;
    }

    public Path getAgentFile() {
        return agentFile;
    }

    public Path getLogPath() {
        return logPath;
    }

    public Pattern getClassPattern() {
        return classPattern;
    }

    public Pattern getCommandPattern() {
        return commandPattern;
    }

    public ClassPool getClassPool() {
        return pool;
    }

    public Collection<Path> getClassPaths() {
        return classPaths;
    }

    public boolean isDebug() {
        return debug;
    }

    public Iterable<URL> findClasses(String className) {
        return classPoolPaths.stream()
                .map(path -> path.find(className))
                .filter(url -> url != null)
                .collect(Collectors.toList());
    }
}
