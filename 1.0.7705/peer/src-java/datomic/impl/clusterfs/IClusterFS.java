package datomic.impl.clusterfs;

import clojure.lang.ILookup;
import java.util.Collection;

/**
 * Read-only view of immutable full-text index files stored as fixed-size
 * chunks. Implementations expose file names and lengths and resolve chunk data
 * through the supplied object lookup.
 */
public interface IClusterFS {
    public byte[] getChunk(ILookup var1, String var2, int var3);

    public Collection getFiles();

    public long fileLength(String var1);

    public int chunkSize();
}
