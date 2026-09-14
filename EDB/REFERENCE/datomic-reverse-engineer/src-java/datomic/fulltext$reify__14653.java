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
import datomic.fulltext.Root;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class fulltext$reify__14653
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public fulltext$reify__14653(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fulltext$reify__14653() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fulltext$reify__14653(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object r = object;
        w.writeTag((Object)"search-root", RT.intCast((long)1L));
        Writer writer2 = w;
        w = null;
        Object object2 = r;
        r = null;
        writer2.writeObject(((Root)object2).attrmap);
    }
}

