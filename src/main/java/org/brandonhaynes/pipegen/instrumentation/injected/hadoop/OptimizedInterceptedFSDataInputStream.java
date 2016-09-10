package org.brandonhaynes.pipegen.instrumentation.injected.hadoop;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.OptimizedInterceptedFileInputStream;

import java.io.IOException;

public class OptimizedInterceptedFSDataInputStream extends FSDataInputStream {
    OptimizedInterceptedFSDataInputStream(Path path) throws IOException {
        super(new OptimizedInterceptedFileInputStream(path.toUri().toString()));
    }
}
