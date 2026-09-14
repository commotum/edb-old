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

public final class uri$fn__17016
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"aws-access-key-id"), (Object)RT.keyword(null, (String)"aws-secret-key"));
    public static final Var const__10 = RT.var((String)"datomic.uri", (String)"query-args");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-s3-path"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5457__auto__17019;
        Object object2;
        Object temp__5457__auto__17018;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        IFn iFn = (IFn)const__3.getRawRoot();
        Object object3 = system_root;
        system_root = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = cluster_conf;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object6 = cluster_conf;
        Object object7 = iLookupThunk2.get(object6);
        if (iLookupThunk2 == object7) {
            __thunk__1__ = __site__1__.fault(object6);
            object7 = __thunk__1__.get(object6);
        }
        Object object8 = temp__5457__auto__17018 = object7;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object db_name;
            Object object9 = temp__5457__auto__17018;
            temp__5457__auto__17018 = null;
            Object object10 = db_name = object9;
            db_name = null;
            object2 = ((IFn)const__3.getRawRoot()).invoke((Object)"/", object10);
        } else {
            object2 = null;
        }
        Object object11 = cluster_conf;
        cluster_conf = null;
        Object object12 = temp__5457__auto__17019 = ((IFn)const__6.getRawRoot()).invoke(object11, (Object)const__9);
        if (object12 != null && object12 != Boolean.FALSE) {
            Object params;
            Object object13 = temp__5457__auto__17019;
            temp__5457__auto__17019 = null;
            Object object14 = params = object13;
            params = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)"?", ((IFn)const__10.getRawRoot()).invoke(object14));
        } else {
            object = null;
        }
        return iFn.invoke((Object)"datomic:s3://", object3, (Object)"/", object5, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17016.invokeStatic(object2);
    }
}

