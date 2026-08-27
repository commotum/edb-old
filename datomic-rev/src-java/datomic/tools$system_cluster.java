/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class tools$system_cluster
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Keyword const__3 = RT.keyword(null, (String)"mem");
    public static final Var const__4 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object protocol;
        Object object2 = uri2;
        uri2 = null;
        Object cluster_conf = ((IFn)const__0.getRawRoot()).invoke(object2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = cluster_conf;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Object object5 = protocol = object4;
        protocol = null;
        if (Util.equiv((Object)object5, (Object)const__3)) {
            object = null;
        } else {
            Object object6 = cluster_conf;
            cluster_conf = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object6);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$system_cluster.invokeStatic(object2);
    }
}

