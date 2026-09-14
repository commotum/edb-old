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

public final class uri$fn__17048
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        boolean or__5238__auto__17050 = ((String)system_root).startsWith(":");
        if (or__5238__auto__17050 ? or__5238__auto__17050 : Util.equiv((Object)system_root, (Object)"")) {
            object = null;
        } else {
            Object object2;
            Object temp__5457__auto__17051;
            IFn iFn = (IFn)const__4.getRawRoot();
            Object object3 = system_root;
            system_root = null;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = cluster_conf;
            cluster_conf = null;
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__0__ = __site__0__.fault(object4);
                object5 = __thunk__0__.get(object4);
            }
            Object object6 = temp__5457__auto__17051 = object5;
            if (object6 != null && object6 != Boolean.FALSE) {
                Object db_name;
                Object object7 = temp__5457__auto__17051;
                temp__5457__auto__17051 = null;
                Object object8 = db_name = object7;
                db_name = null;
                object2 = ((IFn)const__4.getRawRoot()).invoke((Object)"/", object8);
            } else {
                object2 = null;
            }
            object = iFn.invoke((Object)"datomic:mdev://", object3, object2);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17048.invokeStatic(object2);
    }
}

