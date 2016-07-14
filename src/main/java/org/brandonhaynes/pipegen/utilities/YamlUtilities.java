package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlUtilities {
    public static <T> T getChild(Map yaml, String key, Class<T> clazz) throws IOException {
        if(!yaml.containsKey(key))
            throw new IOException("Configuration file does not contain property " + key);
        else
            return getElement(yaml.get(key), clazz);
    }

    public static Collection<Path> getClassPaths(CompileTimeConfiguration configuration, List yaml) {
        Collection<Path> paths = Lists.newArrayList();
        for(Object path: yaml)
            if(path.toString().startsWith("recurse:"))
                paths.addAll(getFilesRecursively(makeAbsolutePath(configuration,
                                                                  path.toString()
                                                                      .substring("recurse:".length())
                                                                      .replace("*", "")),
                             new String[] {"jar", "class"}));
            else
                paths.add(Paths.get(makeAbsolutePath(configuration, path).toString()));
        return paths;
    }

    public static Path makeAbsolutePath(CompileTimeConfiguration configuration, Object path) {
        return path != null
                ? configuration.getBasePath()
                              .resolve(path.toString()
                              .replace("$CONFIG_DIR", configuration.getConfigurationFile().getParent().toString())
                              .replace("BASE_DIR", configuration.getBasePath().toString())
                              .replace("$DIR", System.getProperty("user.dir")))
                : null;
    }

    public static <T> T getElement(Object yaml, Class<T> clazz) throws IOException {
        try {
            return clazz.cast(yaml);
        } catch(ClassCastException e) {
            throw new IOException(String.format("Expected a %s as child element", clazz), e);
        }
    }

    private static Collection<Path> getFilesRecursively(Path path, String[] extensions) {
        return Lists.newArrayList(FileUtils.iterateFiles(path.toFile(), extensions, true))
                .stream()
                .map(File::toPath)
                .collect(Collectors.toList());
    }
}
