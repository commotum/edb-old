/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookup
 */
package datomic.impl.clusterfs;

import clojure.lang.ILookup;
import java.util.Collection;

public interface IClusterFS {
    public byte[] getChunk(ILookup var1, String var2, int var3);

    public Collection getFiles();

    public long fileLength(String var1);

    public int chunkSize();
}

