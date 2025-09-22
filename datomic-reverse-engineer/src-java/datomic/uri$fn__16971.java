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
import clojure.lang.Var;

public final class uri$fn__16971
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"region");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"get-in");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"params"), (Object)RT.keyword(null, (String)"ddb"));
    public static final Var const__10 = RT.var((String)"datomic.uri", (String)"query-args");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5457__auto__16974;
        Object object2;
        Object temp__5457__auto__16973;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        IFn iFn = (IFn)const__3.getRawRoot();
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__4);
        Object object4 = system_root;
        system_root = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = cluster_conf;
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        Object object7 = temp__5457__auto__16973 = object6;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object db_name;
            Object object8 = temp__5457__auto__16973;
            temp__5457__auto__16973 = null;
            Object object9 = db_name = object8;
            db_name = null;
            object2 = ((IFn)const__3.getRawRoot()).invoke((Object)"/", object9);
        } else {
            object2 = null;
        }
        Object object10 = cluster_conf;
        cluster_conf = null;
        Object object11 = temp__5457__auto__16974 = ((IFn)const__6.getRawRoot()).invoke(object10, (Object)const__9);
        if (object11 != null && object11 != Boolean.FALSE) {
            Object ddb_params;
            Object object12 = temp__5457__auto__16974;
            temp__5457__auto__16974 = null;
            Object object13 = ddb_params = object12;
            ddb_params = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)"?", ((IFn)const__10.getRawRoot()).invoke(object13));
        } else {
            object = null;
        }
        return iFn.invoke((Object)"datomic:ddb://", object3, (Object)"/", object4, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16971.invokeStatic(object2);
    }
}

