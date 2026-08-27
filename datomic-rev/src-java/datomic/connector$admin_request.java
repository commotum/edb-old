/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.connector.TransactorConnector;

public final class connector$admin_request
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Object const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object connector, Object request, Object arg, Object timeout_msec) {
        v0 = connector;
        connector = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == connector$admin_request.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof TransactorConnector)) {
            v1 = v1;
            connector$admin_request.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = request;
            request = null;
            v3 = arg;
            arg = null;
            v4 = timeout_msec;
            timeout_msec = null;
            v5 = connector$admin_request.const__2.getRawRoot().invoke(v1, v2, v3, v4);
        } else {
            v6 = request;
            request = null;
            v7 = arg;
            arg = null;
            v8 = timeout_msec;
            timeout_msec = null;
            v5 = ((TransactorConnector)v1).admin_request_STAR_(v6, v7, v8);
        }
        result = v5;
        v9 = connector$admin_request.__thunk__0__;
        v10 = result;
        v11 = v9.get(v10);
        if (v9 == v11) {
            connector$admin_request.__thunk__0__ = connector$admin_request.__site__0__.fault(v10);
            v11 = connector$admin_request.__thunk__0__.get(v10);
        }
        if (v11 != null && v11 != Boolean.FALSE) {
            v12 = (IFn)connector$admin_request.const__4.getRawRoot();
            v13 = connector$admin_request.__thunk__1__;
            v14 = result;
            v15 = v13.get(v14);
            if (v13 == v15) {
                connector$admin_request.__thunk__1__ = connector$admin_request.__site__1__.fault(v14);
                v15 = connector$admin_request.__thunk__1__.get(v14);
            }
            v16 = result;
            result = null;
            throw (Throwable)v12.invoke(v15, ((IFn)connector$admin_request.const__6.getRawRoot()).invoke(v16, (Object)connector$admin_request.const__5));
        }
        v17 = connector$admin_request.__thunk__2__;
        v18 = result;
        result = null;
        v19 = v17.get(v18);
        if (v17 == v19) {
            connector$admin_request.__thunk__2__ = connector$admin_request.__site__2__.fault(v18);
            v19 = connector$admin_request.__thunk__2__.get(v18);
        }
        return v19;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return connector$admin_request.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object connector2, Object request, Object arg2) {
        Object object = connector2;
        connector2 = null;
        Object object2 = request;
        request = null;
        Object object3 = arg2;
        arg2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, const__1);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return connector$admin_request.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.connector", (String)"admin-request");
        const__1 = 60000L;
        const__2 = RT.var((String)"datomic.connector", (String)"admin-request*");
        const__4 = RT.var((String)"clojure.core", (String)"ex-info");
        const__5 = RT.keyword(null, (String)"message");
        const__6 = RT.var((String)"clojure.core", (String)"dissoc");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"error"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"message"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
        __thunk__2__ = __site__2__;
    }
}

