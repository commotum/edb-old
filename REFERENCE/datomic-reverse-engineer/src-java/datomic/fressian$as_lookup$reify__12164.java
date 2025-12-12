/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  org.fressian.handlers.ILookup
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import org.fressian.handlers.ILookup;

public final class fressian$as_lookup$reify__12164
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object o;

    public fressian$as_lookup$reify__12164(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.o = object;
    }

    public fressian$as_lookup$reify__12164(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$as_lookup$reify__12164(iPersistentMap, this.o);
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        fressian$as_lookup$reify__12164 this_ = null;
        return RT.get((Object)this_.o, (Object)object);
    }
}

