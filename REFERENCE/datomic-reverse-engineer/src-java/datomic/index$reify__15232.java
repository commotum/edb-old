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
import datomic.index.TransposedData;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class index$reify__15232
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public index$reify__15232(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public index$reify__15232() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$reify__15232(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object d = object;
        w.writeTag((Object)"index-tdata", RT.uncheckedIntCast((long)5L));
        w.writeObject(((TransposedData)d).vs);
        w.writeObject(((TransposedData)d).getEs());
        w.writeObject(((TransposedData)d).getAs());
        w.writeObject(((TransposedData)d).ts);
        Writer writer2 = w;
        w = null;
        Object object2 = d;
        d = null;
        writer2.writeObject(((TransposedData)object2).ops);
    }
}

