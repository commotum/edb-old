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

public final class peer$rename_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse-db");
    public static final Keyword const__1 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__4 = RT.keyword(null, (String)"mem");
    public static final Var const__5 = RT.var((String)"datomic.peer", (String)"rename-local-database");
    public static final Var const__6 = RT.var((String)"datomic.peer", (String)"send-admin-request");
    public static final Keyword const__7 = RT.keyword(null, (String)"rename-database");
    public static final Keyword const__8 = RT.keyword(null, (String)"new-name");
    public static final Var const__10 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"rename-database-failed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"renamed-to"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object uri2, Object new_name) {
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
        Object db_name = object4;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = cluster_conf;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        Object object7 = protocol = object6;
        protocol = null;
        if (Util.equiv((Object)object7, (Object)const__4)) {
            Object object8 = db_name;
            db_name = null;
            Object object9 = new_name;
            new_name = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object8, object9);
        } else {
            Object object10 = cluster_conf;
            cluster_conf = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            Object object11 = db_name;
            db_name = null;
            objectArray[1] = object11;
            objectArray[2] = const__8;
            Object object12 = new_name;
            new_name = null;
            objectArray[3] = object12;
            Object result2 = ((IFn)const__6.getRawRoot()).invoke(object10, (Object)const__7, (Object)RT.mapUniqueKeys((Object[])objectArray));
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object13 = result2;
            Object object14 = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object14) {
                __thunk__2__ = __site__2__.fault(object13);
                object14 = __thunk__2__.get(object13);
            }
            if (object14 != null && object14 != Boolean.FALSE) {
                object = Boolean.TRUE;
            } else {
                Object object15 = result2;
                result2 = null;
                object = ((IFn)const__10.getRawRoot()).invoke((Object)const__11, (Object)"Unable to rename database", object15);
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$rename_database.invokeStatic(object3, object4);
    }
}

