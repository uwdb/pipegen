package org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_1_2_1;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.server.namenode.NameNode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class InterceptedDFSClient extends DFSClient {
    public InterceptedDFSClient(Configuration conf) throws IOException {
        this(NameNode.getAddress(conf), conf);
    }

    public InterceptedDFSClient(InetSocketAddress nameNodeAddress, Configuration configuration) throws IOException {
        super(nameNodeAddress, configuration);
    }

    public InterceptedDFSClient(InetSocketAddress nameNodeAddress, Configuration configuration,
                                FileSystem.Statistics stats) throws IOException {
        super(nameNodeAddress, configuration, stats);
    }

    public class InterceptedDFSInputStream extends DFSClient.DFSInputStream {
        private final InputStream stream;

        public InterceptedDFSInputStream(String src, int bufferSize, boolean verifyChecksum)
                throws IOException {
            super(src, bufferSize, verifyChecksum);

            try {
                stream = new FileInputStream("/tmp/protobuf");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            stream.close();
        }

        @Override
        public long getFileLength() {
            return super.getFileLength();
        }

        @Override
        public synchronized int read() throws IOException {
            return stream.read();
        }

        @Override
        public synchronized int read(final byte buf[], int off, int len)
                throws IOException {
            return stream.read(buf, off, len);
        }

        @Override
        public int read(long position, byte[] buffer, int offset, int length)
                throws IOException {
            if (position > 0)
                throw new IOException("PipeGen reads do not support position > 0");
            else
                return stream.read(buffer, offset, length);
        }

        @Override
        public long skip(long n) throws IOException {
            throw new IOException("PipeGen does not support skipping.");
        }

        @Override
        public synchronized void seek(long targetPos) throws IOException {
            throw new IOException("PipeGen does not support seeking.");
        }

        @Override
        public synchronized long getPos() {
            return 0;
        }

        @Override
        public synchronized int available() throws IOException {
            return stream.available();
        }
    }
}