/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import datomic.clusterfs.Chunk;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class clusterfs$reify__14238
implements ReadHandler,
IObj {
    final IPersistentMap __meta;

    public clusterfs$reify__14238(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public clusterfs$reify__14238() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new clusterfs$reify__14238(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Reader reader2 = rdr;
        rdr = null;
        return new Chunk(reader2.readObject());
    }
}

