/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class peer$delete_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse-db");
    public static final Keyword const__1 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__4 = RT.keyword(null, (String)"mem");
    public static final Var const__5 = RT.var((String)"datomic.peer", (String)"delete-local-database");
    public static final Var const__6 = RT.var((String)"datomic.coordination", (String)"resolve-db-name");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__10 = RT.keyword(null, (String)"db-id");
    public static final Var const__11 = RT.var((String)"datomic.peer", (String)"connection-lock");
    public static final Var const__12 = RT.var((String)"datomic.peer", (String)"stop-connection");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__14 = RT.var((String)"datomic.peer", (String)"send-admin-request");
    public static final Keyword const__15 = RT.keyword(null, (String)"delete-database");
    public static final Keyword const__16 = RT.keyword(null, (String)"deleted");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__1__ = __site__1__;

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
            object = ((IFn)const__5.getRawRoot()).invoke(object8);
        } else {
            Object temp__5455__auto__21711;
            Object object9 = temp__5455__auto__21711 = ((IFn)const__6.getRawRoot()).invoke(cluster_conf);
            if (object9 != null && object9 != Boolean.FALSE) {
                Object object10;
                Object map__21708;
                Object object11;
                Object object12 = temp__5455__auto__21711;
                temp__5455__auto__21711 = null;
                Object map__217082 = object12;
                Object object13 = ((IFn)const__7.getRawRoot()).invoke(map__217082);
                if (object13 != null && object13 != Boolean.FALSE) {
                    Object object14 = map__217082;
                    map__217082 = null;
                    object11 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__8.getRawRoot()).invoke(object14)));
                } else {
                    object11 = map__217082;
                    map__217082 = null;
                }
                Object resolved_cluster_conf = map__21708 = object11;
                Object object15 = map__21708;
                map__21708 = null;
                RT.get((Object)object15, (Object)const__10);
                Object lockee__5436__auto__21710 = const__11.getRawRoot();
                try {
                    synchronized (lockee__5436__auto__21710) {
                        Object object16 = resolved_cluster_conf;
                        resolved_cluster_conf = null;
                        ((IFn)const__12.getRawRoot()).invoke(object16);
                        Object object17 = cluster_conf;
                        cluster_conf = null;
                        Object[] objectArray = new Object[2];
                        objectArray[0] = const__1;
                        Object object18 = db_name;
                        db_name = null;
                        objectArray[1] = object18;
                        object10 = ((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object17, (Object)const__15, (Object)RT.mapUniqueKeys((Object[])objectArray)), (Object)const__16);
                    }
                }
                finally {
                    Object object19 = lockee__5436__auto__21710;
                    lockee__5436__auto__21710 = null;
                    // ** MonitorExit[v20] (shouldn't be in output)
                }
                {
                    object = object10;
                }
            } else {
                object = Boolean.FALSE;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$delete_database.invokeStatic(object2);
    }
}

