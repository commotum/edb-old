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

public final class peer$create_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"create-database");
    public static final Var const__1 = RT.var((String)"datomic.uri", (String)"parse-db");
    public static final Keyword const__2 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__6 = RT.keyword(null, (String)"mem");
    public static final Var const__7 = RT.var((String)"datomic.peer", (String)"create-local-database");
    public static final Var const__8 = RT.var((String)"datomic.peer", (String)"send-admin-request");
    public static final Keyword const__9 = RT.keyword(null, (String)"create-database");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__13 = RT.keyword(null, (String)"default");
    public static final Var const__14 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"create-database-failed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"uri"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"created"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"exists"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object uri2, Object desc) {
        Object object;
        Object protocol;
        Object object2 = uri2;
        uri2 = null;
        Object cluster_conf = ((IFn)const__1.getRawRoot()).invoke(object2);
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
        Object uri3 = object6;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object7 = cluster_conf;
        Object object8 = iLookupThunk3.get(object7);
        if (iLookupThunk3 == object8) {
            __thunk__2__ = __site__2__.fault(object7);
            object8 = __thunk__2__.get(object7);
        }
        Object object9 = protocol = object8;
        protocol = null;
        if (Util.equiv((Object)object9, (Object)const__6)) {
            Object object10 = db_name;
            db_name = null;
            Object object11 = uri3;
            uri3 = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object10, object11);
        } else {
            Object object12 = cluster_conf;
            cluster_conf = null;
            Object object13 = desc;
            desc = null;
            Object object14 = db_name;
            db_name = null;
            Object result2 = ((IFn)const__8.getRawRoot()).invoke(object12, (Object)const__9, ((IFn)const__10.getRawRoot()).invoke(object13, (Object)const__2, object14));
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object15 = result2;
            Object object16 = iLookupThunk4.get(object15);
            if (iLookupThunk4 == object16) {
                __thunk__3__ = __site__3__.fault(object15);
                object16 = __thunk__3__.get(object15);
            }
            if (object16 != null && object16 != Boolean.FALSE) {
                object = Boolean.TRUE;
            } else {
                ILookupThunk iLookupThunk5 = __thunk__4__;
                Object object17 = result2;
                Object object18 = iLookupThunk5.get(object17);
                if (iLookupThunk5 == object18) {
                    __thunk__4__ = __site__4__.fault(object17);
                    object18 = __thunk__4__.get(object17);
                }
                if (object18 != null && object18 != Boolean.FALSE) {
                    object = Boolean.FALSE;
                } else {
                    Keyword keyword = const__13;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object19 = result2;
                        result2 = null;
                        object = ((IFn)const__14.getRawRoot()).invoke((Object)const__15, (Object)"Unable to create database", object19);
                    } else {
                        object = null;
                    }
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$create_database.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$create_database.invokeStatic(object2);
    }
}

