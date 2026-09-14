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
import datomic.clusterfs.Chunk;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class clusterfs$reify__14234
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public clusterfs$reify__14234(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public clusterfs$reify__14234() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new clusterfs$reify__14234(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object c = object;
        w.writeTag((Object)"clusterfs-chunk", RT.intCast((long)1L));
        Writer writer2 = w;
        w = null;
        Object object2 = c;
        c = null;
        writer2.writeObject(((Chunk)object2).chunk);
    }
}

