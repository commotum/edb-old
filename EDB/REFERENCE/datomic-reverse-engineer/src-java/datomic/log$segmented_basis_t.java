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
import datomic.log.Log;

public final class log$segmented_basis_t
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__5;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object log_value) {
        v0 = log$segmented_basis_t.__thunk__0__;
        v1 = log_value;
        v2 = v0.get(v1);
        if (v0 == v2) {
            log$segmented_basis_t.__thunk__0__ = log$segmented_basis_t.__site__0__.fault(v1);
            v2 = log$segmented_basis_t.__thunk__0__.get(v1);
        }
        olookup = v2;
        v3 = log$segmented_basis_t.__thunk__3__;
        v4 = (IFn)log$segmented_basis_t.const__2.getRawRoot();
        v5 = (IFn)log$segmented_basis_t.const__3.getRawRoot();
        v6 = olookup;
        v7 = log$segmented_basis_t.__thunk__2__;
        v8 = (IFn)log$segmented_basis_t.const__2.getRawRoot();
        v9 = (IFn)log$segmented_basis_t.const__3.getRawRoot();
        v10 = olookup;
        v11 = log$segmented_basis_t.__thunk__1__;
        v12 = (IFn)log$segmented_basis_t.const__2.getRawRoot();
        v13 = (IFn)log$segmented_basis_t.const__3.getRawRoot();
        v14 = olookup;
        olookup = null;
        v15 = log_value;
        log_value = null;
        v16 = v15;
        if (Util.classOf((Object)v15) == log$segmented_basis_t.__cached_class__0) ** GOTO lbl29
        if (!(v16 instanceof Log)) {
            v16 = v16;
            log$segmented_basis_t.__cached_class__0 = Util.classOf((Object)v16);
lbl29:
            // 2 sources

            v17 = log$segmented_basis_t.const__5.getRawRoot().invoke(v16);
        } else {
            v17 = ((Log)v16).get_root_id();
        }
        v18 = v12.invoke(v13.invoke(v14, v17));
        v19 = v11.get(v18);
        if (v11 == v19) {
            log$segmented_basis_t.__thunk__1__ = log$segmented_basis_t.__site__1__.fault(v18);
            v19 = log$segmented_basis_t.__thunk__1__.get(v18);
        }
        v20 = v8.invoke(v9.invoke(v10, v19));
        v21 = v7.get(v20);
        if (v7 == v21) {
            log$segmented_basis_t.__thunk__2__ = log$segmented_basis_t.__site__2__.fault(v20);
            v21 = log$segmented_basis_t.__thunk__2__.get(v20);
        }
        v22 = v4.invoke(v5.invoke(v6, v21));
        v23 = v3.get(v22);
        if (v3 == v23) {
            log$segmented_basis_t.__thunk__3__ = log$segmented_basis_t.__site__3__.fault(v22);
            v23 = log$segmented_basis_t.__thunk__3__.get(v22);
        }
        return v23;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$segmented_basis_t.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"clojure.core", (String)"last");
        const__3 = RT.var((String)"datomic.common", (String)"getx");
        const__5 = RT.var((String)"datomic.log", (String)"get-root-id");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
        __thunk__3__ = __site__3__;
    }
}

