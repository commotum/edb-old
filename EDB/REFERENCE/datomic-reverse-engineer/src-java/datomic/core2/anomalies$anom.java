/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class anomalies$anom
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"category"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"category"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object x, Object context) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__1__;
        Object object2 = x;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__1__ = __site__1__.fault(object2);
            object3 = __thunk__1__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = x;
            x = null;
            Object object5 = context;
            context = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object4, object5);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return anomalies$anom.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object x) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = x;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            object = x;
            x = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return anomalies$anom.invokeStatic(object2);
    }
}

