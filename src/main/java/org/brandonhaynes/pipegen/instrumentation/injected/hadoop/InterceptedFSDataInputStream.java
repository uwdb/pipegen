package org.brandonhaynes.pipegen.instrumentation.injected.hadoop;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileInputStream;

import java.io.IOException;

public class InterceptedFSDataInputStream extends FSDataInputStream {
    public static FSDataInputStream intercept(FileSystem fs, Path path) throws IOException {

        if(!RuntimeConfiguration.getInstance().getFilenamePattern(Direction.IMPORT).matcher(path.toString()).matches())
            return fs.open(path);
        else if(RuntimeConfiguration.getInstance().isOptimized())
            return new OptimizedInterceptedFSDataInputStream(path);
        else
            return new InterceptedFSDataInputStream(path);
    }

    private InterceptedFSDataInputStream(Path path) throws IOException {
        super(new InterceptedFileInputStream(path.toUri().toString()));
    }
}
