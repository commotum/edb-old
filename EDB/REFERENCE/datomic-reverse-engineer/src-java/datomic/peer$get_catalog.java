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

public final class peer$get_catalog
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Keyword const__4 = RT.keyword(null, (String)"mem");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__6 = RT.var((String)"datomic.peer", (String)"local-dbs");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__8 = RT.var((String)"datomic.catalog", (String)"get-catalog");
    public static final Var const__9 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Keyword const__10 = RT.keyword((String)"datomic", (String)"rev");
    public static final Keyword const__11 = RT.keyword((String)"datomic", (String)"deleted");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object uri2) {
        Object object;
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
        Object protocol = object4;
        IFn iFn = (IFn)const__2.getRawRoot();
        Object object5 = protocol;
        protocol = null;
        if (Util.equiv((Object)object5, (Object)const__4)) {
            object = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot());
        } else {
            Object object6 = cluster_conf;
            cluster_conf = null;
            object = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(object6)), (Object)const__10, (Object)const__11);
        }
        return iFn.invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$get_catalog.invokeStatic(object2);
    }
}

