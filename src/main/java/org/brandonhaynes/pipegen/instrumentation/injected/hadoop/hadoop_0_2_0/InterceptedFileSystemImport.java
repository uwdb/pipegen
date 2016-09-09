package org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_0_2_0;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.OptimizedInterceptedFileInputStream;

import java.io.IOException;

public class InterceptedFileSystemImport extends FSDataInputStream {
	public static FSDataInputStream intercept(org.apache.hadoop.fs.FileSystem fs, org.apache.hadoop.fs.Path path)
			throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern(Direction.IMPORT).matcher(path.getName()).matches()
				? new InterceptedFileSystemImport(path)
				: fs.open(path);
	}

	private InterceptedFileSystemImport(Path path) throws IOException {
        super(new SeekableInputStream(new OptimizedInterceptedFileInputStream(path.getName()), 4096));
	}
}
