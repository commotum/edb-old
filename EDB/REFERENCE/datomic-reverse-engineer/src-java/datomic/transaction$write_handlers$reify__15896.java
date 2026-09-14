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

public final class transaction$write_handlers$reify__15896
implements WriteHandler,
IObj {
    final IPersistentMap __meta;
    Object cache;

    public transaction$write_handlers$reify__15896(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.cache = object;
    }

    public transaction$write_handlers$reify__15896(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new transaction$write_handlers$reify__15896(iPersistentMap, this.cache);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object datum2 = object;
        w.writeTag((Object)"datum", RT.intCast((long)6L));
        w.writeBoolean(((IDatum)datum2).isAssertion());
        w.writeObject((Object)RT.intCast((int)((IDatum)datum2).getP()), ((Boolean)this.cache).booleanValue());
        w.writeObject((Object)Numbers.num((long)((IDatumImpl)datum2).eidx()), ((Boolean)this.cache).booleanValue());
        w.writeInt((long)((IDatum)datum2).getA());
        w.writeObject(((IDatum)datum2).getV());
        Writer writer2 = w;
        w = null;
        Object object2 = datum2;
        datum2 = null;
        writer2.writeObject((Object)Numbers.num((long)((IDatum)object2).getT()), ((Boolean)this.cache).booleanValue());
    }
}

