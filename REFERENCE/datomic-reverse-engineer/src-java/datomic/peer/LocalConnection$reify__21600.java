/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Log;

public final class LocalConnection$reify__21600
implements Log,
IObj {
    final IPersistentMap __meta;
    Object memlog;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"tx-range");

    public LocalConnection$reify__21600(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.memlog = object;
        this.db = object2;
    }

    public LocalConnection$reify__21600(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new LocalConnection$reify__21600(iPersistentMap, this.memlog, this.db);
    }

    public Iterable txRange(Object start, Object end) {
        Object object = start;
        start = null;
        Object object2 = end;
        end = null;
        LocalConnection$reify__21600 this_ = null;
        return (Iterable)((IFn)const__0.getRawRoot()).invoke(this_.memlog, this_.db, object, object2);
    }
}

