/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class uri$fn__17011
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"override-endpoint");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"get-in");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"params"), (Object)RT.keyword(null, (String)"ddb-local"));
    public static final Var const__13 = RT.var((String)"datomic.uri", (String)"query-args");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5457__auto__17015;
        Object object2;
        Object temp__5457__auto__17014;
        Object map__17012;
        Object object3;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        Object map__170122 = cluster_conf;
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(map__170122);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__170122;
            map__170122 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object5)));
        } else {
            object3 = map__170122;
            map__170122 = null;
        }
        Object object6 = map__17012 = object3;
        map__17012 = null;
        Object override_endpoint = RT.get((Object)object6, (Object)const__6);
        IFn iFn = (IFn)const__7.getRawRoot();
        Object object7 = override_endpoint;
        override_endpoint = null;
        Object object8 = system_root;
        system_root = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object9 = cluster_conf;
        Object object10 = iLookupThunk.get(object9);
        if (iLookupThunk == object10) {
            __thunk__0__ = __site__0__.fault(object9);
            object10 = __thunk__0__.get(object9);
        }
        Object object11 = temp__5457__auto__17014 = object10;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object db_name;
            Object object12 = temp__5457__auto__17014;
            temp__5457__auto__17014 = null;
            Object object13 = db_name = object12;
            db_name = null;
            object2 = ((IFn)const__7.getRawRoot()).invoke((Object)"/", object13);
        } else {
            object2 = null;
        }
        Object object14 = cluster_conf;
        cluster_conf = null;
        Object object15 = temp__5457__auto__17015 = ((IFn)const__9.getRawRoot()).invoke(object14, (Object)const__12);
        if (object15 != null && object15 != Boolean.FALSE) {
            Object ddb_params;
            Object object16 = temp__5457__auto__17015;
            temp__5457__auto__17015 = null;
            Object object17 = ddb_params = object16;
            ddb_params = null;
            object = ((IFn)const__7.getRawRoot()).invoke((Object)"?", ((IFn)const__13.getRawRoot()).invoke(object17));
        } else {
            object = null;
        }
        return iFn.invoke((Object)"datomic:ddb-local://", object7, (Object)"/", object8, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17011.invokeStatic(object2);
    }
}

