/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookup
 *  com.datomic.lucene.store.Directory
 *  com.datomic.lucene.store.IndexInput
 *  com.datomic.lucene.store.IndexOutput
 *  com.datomic.lucene.store.LockFactory
 *  com.datomic.lucene.store.NoLockFactory
 */
package datomic.impl.lucene;

import clojure.lang.ILookup;
import com.datomic.lucene.store.Directory;
import com.datomic.lucene.store.IndexInput;
import com.datomic.lucene.store.IndexOutput;
import com.datomic.lucene.store.LockFactory;
import com.datomic.lucene.store.NoLockFactory;
import datomic.impl.clusterfs.Closed;
import datomic.impl.clusterfs.IClusterFS;
import datomic.impl.lucene.ClusterIndexInput;
import datomic.impl.lucene.EmptyIndexInput;
import java.io.IOException;
import java.util.Collection;

public class ClusterDirectory
extends Directory {
    private volatile IClusterFS cluster;
    private final ILookup olookup;

    public ClusterDirectory(IClusterFS cluster2, ILookup olookup) throws IOException {
        this.cluster = cluster2;
        this.olookup = olookup;
        this.setLockFactory((LockFactory)NoLockFactory.getNoLockFactory());
    }

    public String[] listAll() throws IOException {
        Collection c = this.cluster.getFiles();
        return (String[])c.toArray(new String[c.size()]);
    }

    public boolean fileExists(String s) throws IOException {
        return this.cluster.getFiles().contains(s);
    }

    public long fileModified(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void touchFile(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void deleteFile(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long fileLength(String s) throws IOException {
        return this.cluster.fileLength(s);
    }

    public IndexOutput createOutput(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public IndexInput openInput(String s) throws IOException {
        if (this.cluster.fileLength(s) == 0L) {
            return new EmptyIndexInput();
        }
        return new ClusterIndexInput(this.cluster, this.olookup, s, this.cluster.chunkSize());
    }

    public void close() throws IOException {
        this.cluster = Closed.instance;
    }
}
