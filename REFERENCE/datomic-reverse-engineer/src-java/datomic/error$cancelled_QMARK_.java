/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;

public final class error$cancelled_QMARK_
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"datomic", (String)"cancelled"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object anom2) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = anom2;
        anom2 = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return object2;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$cancelled_QMARK_.invokeStatic(object2);
    }
}

