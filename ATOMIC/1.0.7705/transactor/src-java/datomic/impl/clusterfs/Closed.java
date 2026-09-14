package datomic.impl.clusterfs;

import clojure.lang.ILookup;
import datomic.impl.clusterfs.IClusterFS;
import java.util.Collection;

/**
 * Singleton sentinel installed after a clustered file system is closed. Every
 * operation fails immediately, preventing a closed full-text index reader from
 * accessing immutable file chunks.
 */
public class Closed
implements IClusterFS {
    public static final IClusterFS instance = new Closed();

    private Closed() {
    }

    @Override
    public byte[] getChunk(ILookup olookup, String file, int i) {
        throw new IllegalStateException("IClusterFS is closed");
    }

    @Override
    public Collection getFiles() {
        throw new IllegalStateException("IClusterFS is closed");
    }

    @Override
    public long fileLength(String file) {
        throw new IllegalStateException("IClusterFS is closed");
    }

    public void deleteFile(String file) {
        throw new IllegalStateException("IClusterFS is closed");
    }

    @Override
    public int chunkSize() {
        throw new IllegalStateException("IClusterFS is closed");
    }
}
