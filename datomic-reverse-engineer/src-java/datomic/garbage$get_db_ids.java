/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$get_db_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__3 = RT.keyword(null, (String)"mem");
    public static final Var const__4 = RT.var((String)"datomic.catalog", (String)"get-catalog");
    public static final Var const__5 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Keyword const__6 = RT.keyword(null, (String)"deleted");
    public static final Keyword const__8 = RT.keyword(null, (String)"active");
    public static final Var const__9 = RT.var((String)"datomic.catalog", (String)"db-ids");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"datomic", (String)"deleted"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object uri2) {
        IPersistentMap iPersistentMap;
        Object protocol;
        Object object = uri2;
        uri2 = null;
        Object cluster_conf = ((IFn)const__0.getRawRoot()).invoke(object);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = protocol = object3;
        protocol = null;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke(object4, (Object)const__3);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = cluster_conf;
            cluster_conf = null;
            Object cat = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object6));
            Object[] objectArray = new Object[4];
            objectArray[0] = const__6;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object7 = cat;
            Object object8 = iLookupThunk2.get(object7);
            if (iLookupThunk2 == object8) {
                __thunk__1__ = __site__1__.fault(object7);
                object8 = __thunk__1__.get(object7);
            }
            objectArray[1] = object8;
            objectArray[2] = const__8;
            Object object9 = cat;
            cat = null;
            objectArray[3] = ((IFn)const__9.getRawRoot()).invoke(object9);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return garbage$get_db_ids.invokeStatic(object2);
    }
}

