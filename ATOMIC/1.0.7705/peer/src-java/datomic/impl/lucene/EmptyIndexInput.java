package datomic.impl.lucene;

import com.datomic.lucene.store.IndexInput;
import java.io.IOException;

/**
 * Zero-length Lucene input used for empty clustered index files. Its position
 * remains at zero, while positive seeks and all read attempts report
 * end-of-file conditions.
 */
public class EmptyIndexInput
extends IndexInput {
    public void close() throws IOException {
    }

    public long getFilePointer() {
        return 0L;
    }

    public void seek(long l) throws IOException {
        if (l < 0L) {
            throw new IllegalArgumentException("Negative seek");
        }
        if (l > 0L) {
            throw new IOException("seek past EOF");
        }
    }

    public long length() {
        return 0L;
    }

    public byte readByte() throws IOException {
        throw new IOException("read past EOF");
    }

    public void readBytes(byte[] bytes, int offset, int len) throws IOException {
        throw new IOException("read past EOF");
    }
}
