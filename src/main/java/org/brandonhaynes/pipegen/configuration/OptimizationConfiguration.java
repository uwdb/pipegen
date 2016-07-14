package org.brandonhaynes.pipegen.configuration;

import org.brandonhaynes.pipegen.utilities.YamlUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.brandonhaynes.pipegen.utilities.YamlUtilities.getChild;

public class OptimizationConfiguration {
    private final Collection<Path> classPaths;

    OptimizationConfiguration(CompileTimeConfiguration configuration, Map yaml) throws IOException {
        this(YamlUtilities.getClassPaths(configuration, getChild(yaml, "classPaths", List.class)));
    }

    private OptimizationConfiguration(Collection<Path> classPaths) {
        this.classPaths = classPaths;
    }

    public Collection<Path> getClassPaths() {
        return classPaths;
    }
}
