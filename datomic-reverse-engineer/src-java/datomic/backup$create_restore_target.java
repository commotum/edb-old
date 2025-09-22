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
import clojure.lang.Var;

public final class backup$create_restore_target
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse-db");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"db-name");
    public static final Var const__5 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Var const__6 = RT.var((String)"datomic.catalog", (String)"create-database");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__10 = RT.var((String)"datomic.backup", (String)"create-cluster");
    public static final Var const__11 = RT.var((String)"datomic.coordination", (String)"cluster-conf->resolved-conf");
    public static final Var const__13 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__14 = RT.keyword((String)"restore", (String)"collision");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__18 = RT.keyword((String)"restore", (String)"invalid-db-name");
    public static final Keyword const__19 = RT.keyword(null, (String)"default");
    public static final Var const__20 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__21 = RT.keyword((String)"restore", (String)"create-db-failed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"exists"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"created"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"name-conflict"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"id-conflict"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"id-conflict"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"invalid-db-name"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object uri2, Object desc, Object concurrency) {
        Object object;
        Object object2;
        Object or__5238__auto__20245;
        Object system_cluster2;
        Object map__20243;
        Object object3;
        Object object4 = uri2;
        uri2 = null;
        Object map__202432 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__202432);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__202432;
            map__202432 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__202432;
            map__202432 = null;
        }
        Object cluster_conf = map__20243 = object3;
        Object object7 = map__20243;
        map__20243 = null;
        Object db_name = RT.get((Object)object7, (Object)const__4);
        Object object8 = system_cluster2 = ((IFn)const__5.getRawRoot()).invoke(cluster_conf);
        system_cluster2 = null;
        Object object9 = desc;
        desc = null;
        Object catalog_resp = ((IFn)const__6.getRawRoot()).invoke(object8, ((IFn)const__7.getRawRoot()).invoke(object9, (Object)const__4, db_name));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object10 = catalog_resp;
        Object object11 = iLookupThunk.get(object10);
        if (iLookupThunk == object11) {
            __thunk__0__ = __site__0__.fault(object10);
            object11 = __thunk__0__.get(object10);
        }
        Object object12 = or__5238__auto__20245 = object11;
        if (object12 != null && object12 != Boolean.FALSE) {
            object2 = or__5238__auto__20245;
            or__5238__auto__20245 = null;
        } else {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object13 = catalog_resp;
            object2 = iLookupThunk2.get(object13);
            if (iLookupThunk2 == object2) {
                __thunk__1__ = __site__1__.fault(object13);
                object2 = __thunk__1__.get(object13);
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object14 = cluster_conf;
            cluster_conf = null;
            Object object15 = concurrency;
            concurrency = null;
            object = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object14), object15);
        } else {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object16 = catalog_resp;
            Object object17 = iLookupThunk3.get(object16);
            if (iLookupThunk3 == object17) {
                __thunk__2__ = __site__2__.fault(object16);
                object17 = __thunk__2__.get(object16);
            }
            if (object17 != null && object17 != Boolean.FALSE) {
                Object object18 = db_name;
                db_name = null;
                object = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, ((IFn)const__15.getRawRoot()).invoke((Object)"The name '", object18, (Object)"' is already in use by a different database"));
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object19 = catalog_resp;
                Object object20 = iLookupThunk4.get(object19);
                if (iLookupThunk4 == object20) {
                    __thunk__3__ = __site__3__.fault(object19);
                    object20 = __thunk__3__.get(object19);
                }
                if (object20 != null && object20 != Boolean.FALSE) {
                    IFn iFn = (IFn)const__13.getRawRoot();
                    IFn iFn2 = (IFn)const__15.getRawRoot();
                    ILookupThunk iLookupThunk5 = __thunk__4__;
                    Object object21 = catalog_resp;
                    catalog_resp = null;
                    Object object22 = iLookupThunk5.get(object21);
                    if (iLookupThunk5 == object22) {
                        __thunk__4__ = __site__4__.fault(object21);
                        object22 = __thunk__4__.get(object21);
                    }
                    object = iFn.invoke((Object)const__14, iFn2.invoke((Object)"The database already exists under the name '", object22, (Object)"'"));
                } else {
                    ILookupThunk iLookupThunk6 = __thunk__5__;
                    Object object23 = catalog_resp;
                    Object object24 = iLookupThunk6.get(object23);
                    if (iLookupThunk6 == object24) {
                        __thunk__5__ = __site__5__.fault(object23);
                        object24 = __thunk__5__.get(object23);
                    }
                    if (object24 != null && object24 != Boolean.FALSE) {
                        Object object25 = db_name;
                        db_name = null;
                        object = ((IFn)const__13.getRawRoot()).invoke((Object)const__18, ((IFn)const__15.getRawRoot()).invoke((Object)"The name '", object25, (Object)"'is an invalid name"));
                    } else {
                        Keyword keyword = const__19;
                        if (keyword != null && keyword != Boolean.FALSE) {
                            Object object26 = catalog_resp;
                            catalog_resp = null;
                            object = ((IFn)const__20.getRawRoot()).invoke((Object)const__21, (Object)"Unable to create database", object26);
                        } else {
                            object = null;
                        }
                    }
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return backup$create_restore_target.invokeStatic(object4, object5, object6);
    }
}

