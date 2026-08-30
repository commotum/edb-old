/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.datomic.lucene.store.IndexInput
 */
package datomic.impl.lucene;

import com.datomic.lucene.store.IndexInput;
import java.io.IOException;

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

