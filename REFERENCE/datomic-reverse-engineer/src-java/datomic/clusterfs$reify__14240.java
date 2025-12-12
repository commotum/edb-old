/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.clusterfs.ClusterFS;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class clusterfs$reify__14240
implements ReadHandler,
IObj {
    final IPersistentMap __meta;

    public clusterfs$reify__14240(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public clusterfs$reify__14240() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new clusterfs$reify__14240(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Object object = rdr.readObject();
        Reader reader2 = rdr;
        rdr = null;
        return new ClusterFS(object, RT.intCast((long)reader2.readInt()));
    }
}

