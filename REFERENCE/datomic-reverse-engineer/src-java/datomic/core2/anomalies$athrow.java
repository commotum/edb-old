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

public final class anomalies$athrow
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"message"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"category"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object anom2) {
        Object object;
        Object or__5581__auto__19458;
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = anom2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = or__5581__auto__19458 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5581__auto__19458;
            or__5581__auto__19458 = null;
        } else {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = anom2;
            object = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object) {
                __thunk__1__ = __site__1__.fault(object5);
                object = __thunk__1__.get(object5);
            }
        }
        Object object6 = anom2;
        anom2 = null;
        throw (Throwable)iFn.invoke(iFn2.invoke(object), object6);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return anomalies$athrow.invokeStatic(object2);
    }
}

