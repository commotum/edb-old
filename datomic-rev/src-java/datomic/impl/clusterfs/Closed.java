/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookup
 */
package datomic.impl.clusterfs;

import clojure.lang.ILookup;
import datomic.impl.clusterfs.IClusterFS;
import java.util.Collection;

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

