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
import datomic.integrity$clusterfs_path_reachability$fn__22408;

public final class integrity$clusterfs_path_reachability
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"dir"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cfs, Object olookup, Object progress) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = olookup;
        olookup = null;
        Object object2 = progress;
        progress = null;
        integrity$clusterfs_path_reachability$fn__22408 integrity$clusterfs_path_reachability$fn__22408 = new integrity$clusterfs_path_reachability$fn__22408(cfs, object, object2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = cfs;
        cfs = null;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        return iFn.invoke((Object)integrity$clusterfs_path_reachability$fn__22408, object4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return integrity$clusterfs_path_reachability.invokeStatic(object4, object5, object6);
    }
}

