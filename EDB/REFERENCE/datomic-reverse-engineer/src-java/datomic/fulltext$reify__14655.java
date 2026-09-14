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
import datomic.fulltext.Root;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class fulltext$reify__14655
implements ReadHandler,
IObj {
    final IPersistentMap __meta;

    public fulltext$reify__14655(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fulltext$reify__14655() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fulltext$reify__14655(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Reader reader2 = rdr;
        rdr = null;
        return new Root(reader2.readObject());
    }
}

