/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$uncached_val_lookup$reify__10654
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object cs;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public cluster$uncached_val_lookup$reify__10654(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.cs = object;
    }

    public cluster$uncached_val_lookup$reify__10654(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new cluster$uncached_val_lookup$reify__10654(iPersistentMap, this.cs);
    }

    /*
     * Unable to fully structure code
     */
    public Object valAt(Object k, Object not_found) {
        v0 = (IFn)cluster$uncached_val_lookup$reify__10654.const__0.getRawRoot();
        v1 = this.cs;
        if (Util.classOf((Object)v1) == cluster$uncached_val_lookup$reify__10654.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            cluster$uncached_val_lookup$reify__10654.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = k;
            k = null;
            v3 = cluster$uncached_val_lookup$reify__10654.const__1.getRawRoot().invoke(v1, v2);
        } else {
            v4 = k;
            k = null;
            v3 = ((ClusteredStore)v1).get_val(v4);
        }
        v5 = ret = v0.invoke(v3);
        if (v5 != null && v5 != Boolean.FALSE) {
            v6 = cluster$uncached_val_lookup$reify__10654.__thunk__0__;
            v7 = ret;
            ret = null;
            v8 = v6.get(v7);
            if (v6 == v8) {
                cluster$uncached_val_lookup$reify__10654.__thunk__0__ = cluster$uncached_val_lookup$reify__10654.__site__0__.fault(v7);
                v8 = cluster$uncached_val_lookup$reify__10654.__thunk__0__.get(v7);
            }
        } else {
            v8 = not_found;
            var2_2 = null;
        }
        return v8;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-val");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__0__ = __site__0__;
    }
}

