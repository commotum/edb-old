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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class uri$parse_db
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-db-uri");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object cluster_conf = ((IFn)const__0.getRawRoot()).invoke(uri2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            object = cluster_conf;
            cluster_conf = null;
        } else {
            Object object4 = uri2;
            uri2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"Invalid database URI ", object4));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$parse_db.invokeStatic(object2);
    }
}

