package org.brandonhaynes.pipegen.instrumentation.injected.filesystem;

import io.netty.buffer.ArrowBuf;
import org.apache.arrow.vector.ValueVector;
import org.apache.arrow.vector.VarCharVector;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.utilities.ColumnUtilities;
import org.brandonhaynes.pipegen.utilities.CompositeVector;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.brandonhaynes.pipegen.utilities.StreamUtilities.convertInteger;

public class OptimizedInterceptedFileOutputStream extends InterceptedFileOutputStream {
    private CompositeVector vector;
    private AugmentedString inferenceEvidence = AugmentedString.empty;

    public OptimizedInterceptedFileOutputStream(File file) throws IOException {
        this(file.getName());
    }

	public OptimizedInterceptedFileOutputStream(String filename) throws IOException {
		super(filename, true);
	}

    OptimizedInterceptedFileOutputStream(OutputStream stream) throws IOException {
        super(stream, true);
    }

    CompositeVector getVector() { return vector; }
    private boolean getIsInferred() { return vector != null; }
    private boolean getIsVectorFull() {
        return vector.getAccessor().getValueCount() > RuntimeConfiguration.getInstance().getVectorSize();
    }

	public void write(AugmentedString value) throws IOException {
        if(!getIsInferred())
            addInferenceEvidence(value);
        else if(!getIsVectorFull())
            addToVector(value);
        else
            writeVector(value);
	}

    private void addToVector(AugmentedString value) {
        vector.getMutator().set(value);
    }
    private void writeVector(AugmentedString value) throws IOException {
        for(ValueVector v: vector.getVectors()) {
            stream.write(convertInteger(v.getAccessor().getValueCount()));
            for(ArrowBuf buffer: v.getBuffers(false)) {
                stream.write(convertInteger(getBufferSize(v, buffer)));
                buffer.getBytes(0, stream, getBufferSize(v, buffer));
            }
        }

        if(value != null)
            write(value);
    }

    private static int getBufferSize(ValueVector vector, ArrowBuf buffer) {
        return vector instanceof VarCharVector && ((VarCharVector)vector).getBuffer() == buffer
                ? ((VarCharVector)vector).getVarByteLength()
                : buffer.readableBytes();
    }

    private void addInferenceEvidence(AugmentedString value) throws IOException  {
        inferenceEvidence = AugmentedString.concat(inferenceEvidence, value);
        if(inferenceEvidence.containsNonNumeric("\n")) {
            vector = ColumnUtilities.createVector(inferenceEvidence);
            vector.allocateNew(RuntimeConfiguration.getInstance().getVarCharSize(),
                               RuntimeConfiguration.getInstance().getVectorSize());
            new InterceptMetadata(filename, vector.getClasses()).write(this.stream);
            write(inferenceEvidence);
        }
    }

	@Override
	public void write(@Nonnull byte[] b) throws IOException {
        write(new AugmentedString(b));
	}

	@Override
	public void write(@Nonnull byte[] b, int off, int len) throws IOException {
        byte[] bytes = new byte[len];
        System.arraycopy(b, off, bytes, 0, len);
        write(new AugmentedString(bytes));
	}

	@Override
	public void write(int b) throws IOException {
        write(new AugmentedString(b));
	}

	@Override
	public void flush() throws IOException {
        if(vector != null)
            writeVector(null);
		stream.flush();
	}

	@Override
	public void	close() throws IOException {
        if(!getIsInferred())
            write(AugmentedString.newline);
        //flush();
		super.close();
        //throw new RuntimeException("lenzzz: " + vector.getVectors().get(0).getClass().getName());
	}
}