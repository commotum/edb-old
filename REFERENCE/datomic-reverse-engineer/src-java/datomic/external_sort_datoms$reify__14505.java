/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import datomic.db.IDatumImpl;
import datomic.impl.db.IDatum;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class external_sort_datoms$reify__14505
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public external_sort_datoms$reify__14505(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public external_sort_datoms$reify__14505() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new external_sort_datoms$reify__14505(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object datum2 = object;
        w.writeTag((Object)"datum", RT.intCast((long)6L));
        w.writeBoolean(((IDatum)datum2).isAssertion());
        w.writeObject((Object)RT.intCast((int)((IDatum)datum2).getP()), Boolean.FALSE.booleanValue());
        w.writeObject((Object)Numbers.num((long)((IDatumImpl)datum2).eidx()), Boolean.FALSE.booleanValue());
        w.writeInt((long)((IDatum)datum2).getA());
        w.writeObject(((IDatum)datum2).getV());
        Writer writer2 = w;
        w = null;
        Object object2 = datum2;
        datum2 = null;
        writer2.writeObject((Object)Numbers.num((long)((IDatum)object2).getT()), Boolean.FALSE.booleanValue());
    }
}

