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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$throw_anom
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-info");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"message"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object anom2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = anom2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = anom2;
        anom2 = null;
        throw (Throwable)iFn.invoke(object2, object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$throw_anom.invokeStatic(object2);
    }
}

