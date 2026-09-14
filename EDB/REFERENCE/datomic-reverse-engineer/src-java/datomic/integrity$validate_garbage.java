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
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.integrity$validate_garbage$fn__22461;

public final class integrity$validate_garbage
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__3;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Object const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Object const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final AFn const__21;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;
    static final KeywordLookupSite __site__4__;
    static ILookupThunk __thunk__4__;

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static Object invokeStatic(Object cr, Object progress) {
        v0 = (IFn)integrity$validate_garbage.const__0.getRawRoot();
        v1 = integrity$validate_garbage.__thunk__0__;
        v2 = cr;
        v3 = v1.get(v2);
        if (v1 == v3) {
            integrity$validate_garbage.__thunk__0__ = integrity$validate_garbage.__site__0__.fault(v2);
            v3 = v4 = integrity$validate_garbage.__thunk__0__.get(v2);
        }
        if (Util.classOf((Object)v3) == integrity$validate_garbage.__cached_class__0) ** GOTO lbl13
        if (!(v4 instanceof ClusteredStore)) {
            v4 = v4;
            integrity$validate_garbage.__cached_class__0 = Util.classOf((Object)v4);
lbl13:
            // 2 sources

            v5 = (IFn)integrity$validate_garbage.const__3.getRawRoot();
            v6 = integrity$validate_garbage.__thunk__1__;
            v7 = cr;
            v8 = v6.get(v7);
            if (v6 == v8) {
                integrity$validate_garbage.__thunk__1__ = integrity$validate_garbage.__site__1__.fault(v7);
                v8 = integrity$validate_garbage.__thunk__1__.get(v7);
            }
            v9 = integrity$validate_garbage.const__1.getRawRoot().invoke(v4, v5.invoke(v8));
        } else {
            v10 = (ClusteredStore)v4;
            v11 = (IFn)integrity$validate_garbage.const__3.getRawRoot();
            v12 = integrity$validate_garbage.__thunk__1__;
            v13 = cr;
            v14 = v12.get(v13);
            if (v12 == v14) {
                integrity$validate_garbage.__thunk__1__ = integrity$validate_garbage.__site__1__.fault(v13);
                v14 = integrity$validate_garbage.__thunk__1__.get(v13);
            }
            v9 = v10.get_ref(v11.invoke(v14));
        }
        v15 = temp__5455__auto__22464 = v0.invoke(v9);
        if (v15 != null && v15 != Boolean.FALSE) {
            v16 = temp__5455__auto__22464;
            temp__5455__auto__22464 = null;
            root_key = v16;
            v17 = integrity$validate_garbage.__thunk__2__;
            v18 = cr;
            v19 = v17.get(v18);
            if (v17 == v19) {
                integrity$validate_garbage.__thunk__2__ = integrity$validate_garbage.__site__2__.fault(v18);
                v19 = integrity$validate_garbage.__thunk__2__.get(v18);
            }
            v20 = integrity$validate_garbage.__thunk__3__;
            v21 = root_key;
            v22 = v20.get(v21);
            if (v20 == v22) {
                integrity$validate_garbage.__thunk__3__ = integrity$validate_garbage.__site__3__.fault(v21);
                v22 = integrity$validate_garbage.__thunk__3__.get(v21);
            }
            root = RT.get((Object)v19, (Object)v22);
            valid_QMARK_ = ((IFn)integrity$validate_garbage.const__7.getRawRoot()).invoke(integrity$validate_garbage.const__8.getRawRoot(), integrity$validate_garbage.const__9.getRawRoot());
            v23 = (IFn)integrity$validate_garbage.const__10.getRawRoot();
            v24 = valid_QMARK_;
            valid_QMARK_ = null;
            v25 = new integrity$validate_garbage$fn__22461(root_key, v24);
            v26 = new Object[4];
            v26[0] = integrity$validate_garbage.const__12;
            v27 = progress;
            progress = null;
            v26[1] = v27;
            v26[2] = integrity$validate_garbage.const__13;
            v26[3] = integrity$validate_garbage.const__14;
            v28 = RT.mapUniqueKeys((Object[])v26);
            v29 = (IFn)integrity$validate_garbage.const__15.getRawRoot();
            v30 = integrity$validate_garbage.__thunk__4__;
            v31 = cr;
            cr = null;
            v32 = v30.get(v31);
            if (v30 == v32) {
                integrity$validate_garbage.__thunk__4__ = integrity$validate_garbage.__site__4__.fault(v31);
                v32 = integrity$validate_garbage.__thunk__4__.get(v31);
            }
            v33 = root;
            root = null;
            result = v23.invoke((Object)v25, integrity$validate_garbage.const__11, (Object)v28, v29.invoke(v32, v33));
            v34 = ((IFn)integrity$validate_garbage.const__16.getRawRoot()).invoke(result);
            if (v34 != null && v34 != Boolean.FALSE) {
                v35 = new Object[8];
                v35[0] = integrity$validate_garbage.const__17;
                v35[1] = Boolean.TRUE;
                v35[2] = integrity$validate_garbage.const__18;
                v35[3] = "Valid garbage sequence";
                v35[4] = integrity$validate_garbage.const__19;
                v36 = root_key;
                root_key = null;
                v35[5] = v36;
                v35[6] = integrity$validate_garbage.const__20;
                v37 = result;
                result = null;
                v35[7] = v37;
                v38 /* !! */  = RT.mapUniqueKeys((Object[])v35);
            } else {
                v38 /* !! */  = result;
                result = null;
            }
        } else {
            v38 /* !! */  = integrity$validate_garbage.const__21;
        }
        return v38 /* !! */ ;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$validate_garbage.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__3 = RT.var((String)"datomic.garbage", (String)"root-ref-key");
        const__7 = RT.var((String)"clojure.core", (String)"comp");
        const__8 = RT.var((String)"clojure.core", (String)"not");
        const__9 = RT.var((String)"clojure.core", (String)"empty?");
        const__10 = RT.var((String)"datomic.integrity", (String)"progress-reduce");
        const__11 = 0L;
        const__12 = RT.keyword(null, (String)"progress");
        const__13 = RT.keyword(null, (String)"n");
        const__14 = 1000L;
        const__15 = RT.var((String)"datomic.garbage", (String)"leaf-seq");
        const__16 = RT.var((String)"clojure.core", (String)"number?");
        const__17 = RT.keyword(null, (String)"valid");
        const__18 = RT.keyword(null, (String)"desc");
        const__19 = RT.keyword(null, (String)"root-key");
        const__20 = RT.keyword(null, (String)"leaves");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"valid"), Boolean.TRUE, RT.keyword(null, (String)"root-key"), null});
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
        __thunk__4__ = __site__4__;
    }
}

