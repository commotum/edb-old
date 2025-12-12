/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class index$conjable_on_channel$reify__15423
implements IPersistentCollection,
IObj {
    final IPersistentMap __meta;
    Object ch;
    public static final Var const__1 = RT.var((String)"clojure.core.async", (String)">!!");

    public index$conjable_on_channel$reify__15423(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.ch = object;
    }

    public index$conjable_on_channel$reify__15423(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$conjable_on_channel$reify__15423(iPersistentMap, this.ch);
    }

    public IPersistentCollection cons(Object o) {
        Object object = o;
        o = null;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(this.ch, object);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)new IllegalStateException("Channel closed");
        }
        return this;
    }

    public boolean equiv(Object o) {
        Object object = o;
        o = null;
        return Util.identical((Object)this, (Object)object);
    }
}

