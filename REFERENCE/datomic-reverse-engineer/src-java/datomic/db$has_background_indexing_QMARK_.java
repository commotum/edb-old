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

public final class db$has_background_indexing_QMARK_
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db2;
        db2 = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return RT.booleanCast((Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$has_background_indexing_QMARK_.invokeStatic(object2);
    }
}

