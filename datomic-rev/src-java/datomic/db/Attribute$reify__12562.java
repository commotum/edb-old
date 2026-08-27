/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class Attribute$reify__12562
implements ILookupThunk,
IObj {
    final IPersistentMap __meta;
    Object gclass;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"class");

    public Attribute$reify__12562(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.gclass = object;
    }

    public Attribute$reify__12562(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new Attribute$reify__12562(iPersistentMap, this.gclass);
    }

    public Object get(Object gtarget) {
        Object object;
        if (Util.identical((Object)((IFn)const__1.getRawRoot()).invoke(gtarget), (Object)this.gclass)) {
            Object object2 = gtarget;
            gtarget = null;
            object = ((Attribute)object2).needsAVET;
        } else {
            object = this;
        }
        return object;
    }
}

