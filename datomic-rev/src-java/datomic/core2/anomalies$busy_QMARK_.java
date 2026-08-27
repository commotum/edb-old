/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;

public final class anomalies$busy_QMARK_
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"busy");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"cognitect.anomalies", (String)"category"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object x) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = x;
        x = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return Util.equiv((Object)const__1, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return anomalies$busy_QMARK_.invokeStatic(object2);
    }
}

