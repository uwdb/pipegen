package org.brandonhaynes.pipegen.instrumentation.injected.hadoop;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Progressable;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.OptimizedInterceptedFileOutputStream;

import java.io.IOException;

public class InterceptedFSDataOutputStream extends FSDataOutputStream {
	public static FSDataOutputStream intercept(FileSystem fs, Path path, Progressable progress)
			throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(path.getName()).matches()
				? new InterceptedFSDataOutputStream(path)
				: fs.create(path, progress);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path)
			throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(path.getName()).matches()
				? new InterceptedFSDataOutputStream(path)
				: fs.create(path);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path, boolean overwrite, int bufferSize,
										short replication, long blockSize, Progressable progress) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(path.getName()).matches()
				? new InterceptedFSDataOutputStream(path)
				: fs.create(path, overwrite, bufferSize, replication, blockSize, progress);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path, boolean overwrite, int bufferSize,
										short replication, long blockSize) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(path.getName()).matches()
				? new InterceptedFSDataOutputStream(path)
				: fs.create(path, overwrite, bufferSize, replication, blockSize);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path, boolean overwrite) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern(Direction.EXPORT).matcher(path.getName()).matches()
				? new InterceptedFSDataOutputStream(path)
				: fs.create(path, overwrite);
	}

	private InterceptedFSDataOutputStream(Path path) throws IOException {
		super(new OptimizedInterceptedFileOutputStream(path.toUri().toString()), null);
	}
}
