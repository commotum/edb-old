/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class integrity$describe_segment
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Var const__7;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri, Object segment_id) {
        v0 = uri;
        uri = null;
        v1 = uri = ((IFn)integrity$describe_segment.const__0.getRawRoot()).invoke(v0);
        uri = null;
        cr = ((IFn)integrity$describe_segment.const__1.getRawRoot()).invoke(v1);
        v2 = integrity$describe_segment.__thunk__1__;
        v3 = (IFn)integrity$describe_segment.const__3.getRawRoot();
        v4 = integrity$describe_segment.__thunk__0__;
        v5 = cr;
        cr = null;
        v6 = v4.get(v5);
        if (v4 == v6) {
            integrity$describe_segment.__thunk__0__ = integrity$describe_segment.__site__0__.fault(v5);
            v6 = v7 = integrity$describe_segment.__thunk__0__.get(v5);
        }
        if (Util.classOf((Object)v6) == integrity$describe_segment.__cached_class__0) ** GOTO lbl20
        if (!(v7 instanceof ClusteredStore)) {
            v7 = v7;
            integrity$describe_segment.__cached_class__0 = Util.classOf((Object)v7);
lbl20:
            // 2 sources

            v8 = segment_id;
            segment_id = null;
            v9 = integrity$describe_segment.const__4.getRawRoot().invoke(v7, ((IFn)integrity$describe_segment.const__6.getRawRoot()).invoke(v8));
        } else {
            v10 = segment_id;
            segment_id = null;
            v9 = ((ClusteredStore)v7).get_val(((IFn)integrity$describe_segment.const__6.getRawRoot()).invoke(v10));
        }
        v11 = v3.invoke(v9);
        v12 = v2.get(v11);
        if (v2 == v12) {
            integrity$describe_segment.__thunk__1__ = integrity$describe_segment.__site__1__.fault(v11);
            v12 = integrity$describe_segment.__thunk__1__.get(v11);
        }
        v13 = buf = v12;
        buf = null;
        return ((IFn)integrity$describe_segment.const__7.getRawRoot()).invoke(v13);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$describe_segment.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
        const__1 = RT.var((String)"datomic.tools", (String)"connection-resources");
        const__3 = RT.var((String)"clojure.core", (String)"deref");
        const__4 = RT.var((String)"datomic.cluster", (String)"get-val");
        const__6 = RT.var((String)"clojure.core", (String)"str");
        const__7 = RT.var((String)"datomic.io", (String)"describe-bbuf");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__1__ = __site__1__;
    }
}

