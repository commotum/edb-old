/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$local_tuple$fn__13917;

public final class db$local_tuple
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map-indexed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleRefOffsets"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object attr, Object v, Object db2, Object procargs, Object local_tempids) {
        Object result2;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = attr;
        attr = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object ref_offset_QMARK_ = object2;
        Object object3 = procargs;
        procargs = null;
        Object object4 = ref_offset_QMARK_;
        ref_offset_QMARK_ = null;
        Object object5 = db2;
        db2 = null;
        Object object6 = local_tempids;
        local_tempids = null;
        Object object7 = v;
        v = null;
        Object object8 = result2 = ((IFn)const__1.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__2.getRawRoot()).invoke((Object)new db$local_tuple$fn__13917(object3, object4, object5, object6)), object7);
        result2 = null;
        return object8;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return db$local_tuple.invokeStatic(object6, object7, object8, object9, object10);
    }
}

