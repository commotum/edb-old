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
import clojure.lang.Var;
import datomic.backup$uncached_storage_lookup$reify__20046$fn__20047;

public final class backup$uncached_storage_lookup$reify__20046
implements ILookup,
IObj {
    final IPersistentMap __meta;
    Object storage;
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"retry");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public backup$uncached_storage_lookup$reify__20046(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.storage = object;
    }

    public backup$uncached_storage_lookup$reify__20046(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new backup$uncached_storage_lookup$reify__20046(iPersistentMap, this.storage);
    }

    public Object valAt(Object k, Object not_found) {
        Object object;
        Object ret;
        Object object2 = k;
        k = null;
        Object object3 = ret = ((IFn)const__0.getRawRoot()).invoke((Object)new backup$uncached_storage_lookup$reify__20046$fn__20047(this.storage, object2));
        if (object3 != null && object3 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = ret;
            ret = null;
            object = iLookupThunk.get(object4);
            if (iLookupThunk == object) {
                __thunk__0__ = __site__0__.fault(object4);
                object = __thunk__0__.get(object4);
            }
        } else {
            object = not_found;
            Object var2_2 = null;
        }
        return object;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }
}

