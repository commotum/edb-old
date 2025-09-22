/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datalog$bound_consts$fn__18856;

public final class datalog$bound_consts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce-kv");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"in-consts"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object srcs, Object query2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = srcs;
        srcs = null;
        datalog$bound_consts$fn__18856 datalog$bound_consts$fn__18856 = new datalog$bound_consts$fn__18856(object);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = query2;
        query2 = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke((Object)datalog$bound_consts$fn__18856, (Object)PersistentArrayMap.EMPTY, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$bound_consts.invokeStatic(object3, object4);
    }
}

