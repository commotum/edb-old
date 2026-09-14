/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.clusterfs.ClusterFS;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class clusterfs$reify__14236
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public clusterfs$reify__14236(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public clusterfs$reify__14236() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new clusterfs$reify__14236(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object cfs = object;
        w.writeTag((Object)"clusterfs-root", RT.intCast((long)2L));
        w.writeObject(((ClusterFS)cfs).dir);
        Writer writer2 = w;
        w = null;
        Object object2 = cfs;
        cfs = null;
        writer2.writeObject((Object)((ClusterFS)object2).chunkSize());
    }
}

