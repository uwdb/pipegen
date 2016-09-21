package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Stream;

public class PathUtilities {
    private static final String pipegenJarPattern = ".*/(original-)?pipegen-.*\\.jar$";

    public static String resolveFilename(String filename) {
        try {
            URI uri = new URI(URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
            if (uri.getScheme() == null || uri.getScheme().equals("file"))
                return URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8.name());
            else
                throw new RuntimeException("Scheme not supported: " + uri.getScheme());
        } catch(URISyntaxException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Path> getJavaFiles(Path fileOrDirectory) {
        return getJavaFiles(fileOrDirectory, true);
    }

    public static Stream<Path> getJavaFiles(Path fileOrDirectory, boolean excludePipegenJar) {
        return getFileOrFiles(fileOrDirectory, "glob:*.{jar,class,java}").filter(p ->
                !excludePipegenJar || !p.toString().matches(pipegenJarPattern));
    }

    public static Stream<Path> getFileOrFiles(Path fileOrDirectory) {
        return getFileOrFiles(fileOrDirectory, "glob:*");
    }

    public static Stream<Path> getFileOrFiles(Path fileOrDirectory, String filterExpression) {
        File[] files;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(filterExpression);

        if(fileOrDirectory.getFileName().toString().contains("*"))
            return Lists.newArrayList(fileOrDirectory.toFile()
                    .getParentFile()
                    .listFiles((FileFilter)new WildcardFileFilter(
                            fileOrDirectory.getFileName().toString())))
                    .stream()
                    .map(File::toPath)
                    .filter(p -> matcher.matches(p.getFileName()));
        else if(fileOrDirectory.toFile().isDirectory() && (files = fileOrDirectory.toFile().listFiles()) != null)
            return Lists.newArrayList(files)
                        .stream()
                        .map(File::toPath)
                        .filter(p -> p.toFile().isDirectory() || matcher.matches(p.getFileName()));
        else
            return Lists.newArrayList(new Path[] {fileOrDirectory}).stream();
    }
}
