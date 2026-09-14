/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$ensure_root
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.garbage", (String)"ensure-root-ref");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__3 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster2, Object lookup) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = cluster2;
        cluster2 = null;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(object);
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object key = object3;
        Object object4 = lookup;
        lookup = null;
        Object object5 = key;
        key = null;
        return ((IFn)const__2.getRawRoot()).invoke(object4, ((IFn)const__3.getRawRoot()).invoke(object5));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$ensure_root.invokeStatic(object3, object4);
    }
}

