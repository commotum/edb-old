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

public final class peer$connect_uri
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.peer", (String)"initialize");
    public static final Var const__2 = RT.var((String)"datomic.uri", (String)"parse-db");
    public static final Keyword const__5 = RT.keyword(null, (String)"mem");
    public static final Var const__6 = RT.var((String)"datomic.peer", (String)"connect-local-database");
    public static final Var const__8 = RT.var((String)"datomic.peer", (String)"get-connection");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object protocol;
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        Object object2 = uri2;
        uri2 = null;
        Object cluster_conf = ((IFn)const__2.getRawRoot()).invoke(object2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = cluster_conf;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Object object5 = protocol = object4;
        protocol = null;
        if (Util.equiv((Object)object5, (Object)const__5)) {
            IFn iFn = (IFn)const__6.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object6 = cluster_conf;
            cluster_conf = null;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__1__ = __site__1__.fault(object6);
                object7 = __thunk__1__.get(object6);
            }
            object = iFn.invoke(object7);
        } else {
            Object object8 = cluster_conf;
            cluster_conf = null;
            object = ((IFn)const__8.getRawRoot()).invoke(object8);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$connect_uri.invokeStatic(object2);
    }
}

