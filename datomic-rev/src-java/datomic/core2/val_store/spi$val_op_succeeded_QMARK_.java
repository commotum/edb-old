/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;

public final class spi$val_op_succeeded_QMARK_
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"result"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object store_api_result) {
        Object object;
        Object or__5581__auto__21854;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = store_api_result;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = or__5581__auto__21854 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5581__auto__21854;
            or__5581__auto__21854 = null;
        } else {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = store_api_result;
            store_api_result = null;
            object = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object) {
                __thunk__1__ = __site__1__.fault(object5);
                object = __thunk__1__.get(object5);
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return spi$val_op_succeeded_QMARK_.invokeStatic(object2);
    }
}

