/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class uri$create_h2
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"name");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"h2-port"), 4335L});
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"h2-port"));
    public static final Var const__13 = RT.var((String)"datomic.uri", (String)"map->query-string");
    public static final AFn const__14 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"h2-port"));
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        boolean or__5238__auto__17042 = ((String)system_root).startsWith(":");
        if (or__5238__auto__17042 ? or__5238__auto__17042 : Util.equiv((Object)system_root, (Object)"")) {
            object = null;
        } else {
            Object object2;
            Object object3;
            Object temp__5457__auto__17043;
            IFn iFn = (IFn)const__4.getRawRoot();
            IFn iFn2 = (IFn)const__5.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = cluster_conf;
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__0__ = __site__0__.fault(object4);
                object5 = __thunk__0__.get(object4);
            }
            Object object6 = iFn2.invoke(object5);
            Object object7 = system_root;
            system_root = null;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object8 = cluster_conf;
            Object object9 = iLookupThunk2.get(object8);
            if (iLookupThunk2 == object9) {
                __thunk__1__ = __site__1__.fault(object8);
                object9 = __thunk__1__.get(object8);
            }
            Object object10 = temp__5457__auto__17043 = object9;
            if (object10 != null && object10 != Boolean.FALSE) {
                Object db_name;
                Object object11 = temp__5457__auto__17043;
                temp__5457__auto__17043 = null;
                Object object12 = db_name = object11;
                db_name = null;
                object3 = ((IFn)const__4.getRawRoot()).invoke((Object)"/", object12);
            } else {
                object3 = null;
            }
            if (Util.equiv((Object)const__10, (Object)((IFn)const__11.getRawRoot()).invoke(cluster_conf, (Object)const__12))) {
                object2 = null;
            } else {
                Object object13 = cluster_conf;
                cluster_conf = null;
                object2 = ((IFn)const__4.getRawRoot()).invoke((Object)"?", ((IFn)const__13.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object13, (Object)const__14)));
            }
            object = iFn.invoke((Object)"datomic:", object6, (Object)"://", object7, object3, object2);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$create_h2.invokeStatic(object2);
    }
}

