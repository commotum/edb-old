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
import java.util.Map;

public final class peer$get_database_names
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__5 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__6 = RT.keyword((String)"db.error", (String)"invalid-db-uri");
    public static final Var const__7 = RT.var((String)"datomic.peer", (String)"get-catalog");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object uri2) {
        Object cluster_conf = ((IFn)const__0.getRawRoot()).invoke(uri2);
        if (uri2 instanceof Map) {
            Object object;
            Object or__5238__auto__21716;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object2 = cluster_conf;
            cluster_conf = null;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            Object object4 = or__5238__auto__21716 = object3;
            if (object4 != null && object4 != Boolean.FALSE) {
                object = or__5238__auto__21716;
                or__5238__auto__21716 = null;
            } else {
                object = "*";
            }
            if (Util.equiv((Object)object, (Object)"*")) {
            } else {
                ((IFn)const__5.getRawRoot()).invoke((Object)const__6, (Object)"Invalid URI. Note :db-name should be omitted from connection map");
            }
        } else {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object = cluster_conf;
            cluster_conf = null;
            Object object5 = iLookupThunk.get(object);
            if (iLookupThunk == object5) {
                __thunk__1__ = __site__1__.fault(object);
                object5 = __thunk__1__.get(object);
            }
            if (Util.equiv((Object)object5, (Object)"*")) {
            } else {
                ((IFn)const__5.getRawRoot()).invoke((Object)const__6, (Object)"Invalid URI. Note URI must have '*' in place of database name.");
            }
        }
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__7.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$get_database_names.invokeStatic(object2);
    }
}

