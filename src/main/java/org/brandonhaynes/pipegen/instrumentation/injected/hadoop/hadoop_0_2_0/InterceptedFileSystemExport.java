package org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_0_2_0;

import org.apache.hadoop.fs.ChecksumFileSystem;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Progressable;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileInputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class InterceptedFileSystemExport extends FSDataOutputStream {
	public static FSDataOutputStream intercept(FileSystem fs, Path path, Progressable progress)
			throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(path.getName()).matches()
				? new InterceptedFileSystemExport(path)
				: fs.create(path, progress);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path)
			throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(path.getName()).matches()
				? new InterceptedFileSystemExport(path)
				: fs.create(path);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path, boolean overwrite, int bufferSize,
										short replication, long blockSize, Progressable progress) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(path.getName()).matches()
				? new InterceptedFileSystemExport(path)
				: fs.create(path, overwrite, bufferSize, replication, blockSize, progress);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path, boolean overwrite, int bufferSize,
										short replication, long blockSize) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(path.getName()).matches()
				? new InterceptedFileSystemExport(path)
				: fs.create(path, overwrite, bufferSize, replication, blockSize);
	}

	public static FSDataOutputStream intercept(FileSystem fs, Path path, boolean overwrite) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(path.getName()).matches()
				? new InterceptedFileSystemExport(path)
				: fs.create(path, overwrite);
	}

	public static Collection<Class> getDependencies() {
		return new ArrayList<Class>() {{
			add(InterceptedFileSystemExport.class);
			addAll(InterceptedFileOutputStream.getDependencies());
		}};
	}

	private InterceptedFileSystemExport(Path path) throws IOException {
		super(new InterceptedFileOutputStream(path.toUri().toString()), null);
	}
}
