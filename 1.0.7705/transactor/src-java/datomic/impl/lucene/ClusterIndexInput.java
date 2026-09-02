package datomic.impl.lucene;

import clojure.lang.ILookup;
import com.datomic.lucene.store.IndexInput;
import datomic.impl.clusterfs.Closed;
import datomic.impl.clusterfs.IClusterFS;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Seekable Lucene input over fixed-size clustered file chunks. Chunks are
 * loaded on demand through {@code IClusterFS}; cloned inputs duplicate the
 * current byte buffer so readers can advance independently.
 */
public class ClusterIndexInput
extends IndexInput {
    private final int chunkSize;
    private volatile IClusterFS cluster;
    private final ILookup olookup;
    private final String filename;
    private int chunkNo;
    private ByteBuffer chunk;

    public ClusterIndexInput(IClusterFS cluster2, ILookup olookup, String filename, int chunkSize) throws IOException {
        this.cluster = cluster2;
        this.olookup = olookup;
        this.filename = filename;
        this.chunkSize = chunkSize;
        this.chunkNo = -1;
        this.seek(0L);
    }

    public Object clone() {
        ClusterIndexInput clone = (ClusterIndexInput)((Object)super.clone());
        clone.chunk = clone.chunk.duplicate();
        return clone;
    }

    public void close() throws IOException {
        this.cluster = Closed.instance;
    }

    public long getFilePointer() {
        return (long)this.chunkNo * (long)this.chunkSize + (long)this.chunk.position();
    }

    public void seek(long l) throws IOException {
        int newChunkNo;
        long fileLen = this.cluster.fileLength(this.filename);
        if (0L <= l) {
            if (l > fileLen) {
                l = fileLen;
                newChunkNo = (int)((l - 1L) / (long)this.chunkSize);
            } else {
                newChunkNo = (int)(l / (long)this.chunkSize);
            }
            if (newChunkNo != this.chunkNo) {
                this.chunkNo = newChunkNo;
                this.chunk = ByteBuffer.wrap(this.cluster.getChunk(this.olookup, this.filename, this.chunkNo));
                if (this.chunkNo < (int)(this.length() / (long)this.chunkSize) && this.chunk.remaining() != this.chunkSize) {
                    throw new IllegalStateException("Invalid chunk size " + this.chunk.remaining() + "/" + this.length());
                }
            }
            if (l == fileLen) {
                this.chunk.position(this.chunk.limit());
            } else {
                this.chunk.position((int)(l % (long)this.chunkSize));
            }
        } else {
            throw new IOException("Offset " + l + " out of range in [" + this.filename + " " + this.cluster.fileLength(this.filename) + "]");
        }
    }

    public long length() {
        return this.cluster.fileLength(this.filename);
    }

    private void advanceChunk() throws IOException {
        if (!this.chunk.hasRemaining()) {
            this.seek(this.getFilePointer());
        }
        if (!this.chunk.hasRemaining()) {
            throw new IOException("End of stream in [" + this.filename + " " + this.cluster.fileLength(this.filename) + "] at " + this.getFilePointer());
        }
    }

    public byte readByte() throws IOException {
        this.advanceChunk();
        return this.chunk.get();
    }

    public void readBytes(byte[] bytes, int offset, int len) throws IOException {
        while (len > 0) {
            this.advanceChunk();
            int size = Math.min(this.chunk.remaining(), len);
            this.chunk.get(bytes, offset, size);
            offset += size;
            len -= size;
        }
    }
}
