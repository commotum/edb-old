/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class uri$loggable_cluster_conf
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Keyword const__3 = RT.keyword(null, (String)"system-root");
    public static final AFn const__8 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"protocol"), RT.keyword(null, (String)"db-name"), RT.keyword(null, (String)"system-root"), RT.keyword(null, (String)"host"), RT.keyword(null, (String)"port"), RT.keyword(null, (String)"bucket"), RT.keyword(null, (String)"db-id")});
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__10 = RT.var((String)"datomic.uri", (String)"remove-query-string");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"system-root"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object m;
        Object object2 = cluster_conf;
        cluster_conf = null;
        Object G__16955 = m = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)const__8);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = m;
        m = null;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = G__16955;
            G__16955 = null;
            object = ((IFn)const__9.getRawRoot()).invoke(object5, (Object)const__3, const__10.getRawRoot());
        } else {
            object = G__16955;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$loggable_cluster_conf.invokeStatic(object2);
    }
}

