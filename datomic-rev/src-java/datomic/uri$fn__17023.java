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

public final class uri$fn__17023
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"get-in");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"params"), (Object)RT.keyword(null, (String)"ddb"));
    public static final Var const__8 = RT.var((String)"datomic.uri", (String)"query-args");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5457__auto__17026;
        Object object2;
        Object temp__5457__auto__17025;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = cluster_conf;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        Object object6 = temp__5457__auto__17025 = object5;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object db_name;
            Object object7 = temp__5457__auto__17025;
            temp__5457__auto__17025 = null;
            Object object8 = db_name = object7;
            db_name = null;
            object2 = ((IFn)const__0.getRawRoot()).invoke((Object)"/", object8);
        } else {
            object2 = null;
        }
        Object object9 = cluster_conf;
        cluster_conf = null;
        Object object10 = temp__5457__auto__17026 = ((IFn)const__4.getRawRoot()).invoke(object9, (Object)const__7);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object ddb_params;
            Object object11 = temp__5457__auto__17026;
            temp__5457__auto__17026 = null;
            Object object12 = ddb_params = object11;
            ddb_params = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)"?", ((IFn)const__8.getRawRoot()).invoke(object12));
        } else {
            object = null;
        }
        return iFn.invoke((Object)"datomic:olddev://", object3, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17023.invokeStatic(object2);
    }
}

