/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class log$create_log_val
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final AFn const__2;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__11;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs, Object olookup, Object db) {
        v0 = cs;
        if (v0 == null || v0 == Boolean.FALSE) {
            throw (Throwable)new AssertionError(((IFn)log$create_log_val.const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)log$create_log_val.const__1.getRawRoot()).invoke((Object)log$create_log_val.const__2)));
        }
        v1 = olookup;
        if (v1 == null || v1 == Boolean.FALSE) {
            throw (Throwable)new AssertionError(((IFn)log$create_log_val.const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)log$create_log_val.const__1.getRawRoot()).invoke((Object)log$create_log_val.const__3)));
        }
        v2 = db;
        if (v2 == null || v2 == Boolean.FALSE) {
            throw (Throwable)new AssertionError(((IFn)log$create_log_val.const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)log$create_log_val.const__1.getRawRoot()).invoke((Object)log$create_log_val.const__4)));
        }
        v3 = (IFn)log$create_log_val.const__5.getRawRoot();
        v4 = cs;
        if (Util.classOf((Object)v4) == log$create_log_val.__cached_class__0) ** GOTO lbl16
        if (!(v4 instanceof ClusteredStore)) {
            v4 = v4;
            log$create_log_val.__cached_class__0 = Util.classOf((Object)v4);
lbl16:
            // 2 sources

            v5 = log$create_log_val.const__6.getRawRoot().invoke(v4, ((IFn)log$create_log_val.const__7.getRawRoot()).invoke(cs));
        } else {
            v5 = ((ClusteredStore)v4).get_pod_meta(((IFn)log$create_log_val.const__7.getRawRoot()).invoke(cs));
        }
        v6 = temp__5457__auto__16517 = v3.invoke(v5);
        if (v6 != null && v6 != Boolean.FALSE) {
            v7 = temp__5457__auto__16517;
            temp__5457__auto__16517 = null;
            v8 = desc = v7;
            desc = null;
            v9 = cs;
            cs = null;
            desc = ((IFn)log$create_log_val.const__8.getRawRoot()).invoke(v8, v9);
            v10 = (IFn)log$create_log_val.const__9.getRawRoot();
            v11 = log$create_log_val.__thunk__0__;
            v12 = desc;
            desc = null;
            v13 = v11.get(v12);
            if (v11 == v13) {
                log$create_log_val.__thunk__0__ = log$create_log_val.__site__0__.fault(v12);
                v13 = log$create_log_val.__thunk__0__.get(v12);
            }
            root_id = v10.invoke(v13);
            v14 = (IFn)log$create_log_val.const__11.getRawRoot();
            v15 = db;
            v16 = olookup;
            olookup = null;
            v17 = root_id;
            root_id = null;
            v18 = log$create_log_val.__thunk__1__;
            v19 = db;
            db = null;
            v20 = v18.get(v19);
            if (v18 == v20) {
                log$create_log_val.__thunk__1__ = log$create_log_val.__site__1__.fault(v19);
                v20 = log$create_log_val.__thunk__1__.get(v19);
            }
            v21 = v14.invoke(v15, v16, v17, v20);
        } else {
            v21 = null;
        }
        return v21;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$create_log_val.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"str");
        const__1 = RT.var((String)"clojure.core", (String)"pr-str");
        const__2 = (AFn)Symbol.intern(null, (String)"cs");
        const__3 = (AFn)Symbol.intern(null, (String)"olookup");
        const__4 = (AFn)Symbol.intern(null, (String)"db");
        const__5 = RT.var((String)"clojure.core", (String)"deref");
        const__6 = RT.var((String)"datomic.cluster", (String)"get-pod-meta");
        const__7 = RT.var((String)"datomic.log", (String)"tail-pod-key");
        const__8 = RT.var((String)"datomic.log", (String)"normalize-desc");
        const__9 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
        const__11 = RT.var((String)"datomic.log", (String)"->LogValue");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"memlog"));
        __thunk__1__ = __site__1__;
    }
}

