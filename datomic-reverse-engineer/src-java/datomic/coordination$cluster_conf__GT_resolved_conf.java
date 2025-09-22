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

public final class coordination$cluster_conf__GT_resolved_conf
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"parse-db-conf");
    public static final Var const__2 = RT.var((String)"datomic.catalog", (String)"get-catalog");
    public static final Var const__3 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"merge");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5457__auto__11717;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(cluster_conf));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = cluster_conf;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Object object5 = temp__5457__auto__11717 = iFn.invoke(RT.get((Object)object2, (Object)object4));
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = temp__5457__auto__11717;
            temp__5457__auto__11717 = null;
            Object db_specific = object6;
            Object object7 = cluster_conf;
            cluster_conf = null;
            Object object8 = db_specific;
            db_specific = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object7, object8);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$cluster_conf__GT_resolved_conf.invokeStatic(object2);
    }
}

