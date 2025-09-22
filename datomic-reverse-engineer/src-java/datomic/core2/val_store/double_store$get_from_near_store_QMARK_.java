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
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class double_store$get_from_near_store_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"datomic.core2.val-store.opts", (String)"reset-cache"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"datomic.core2.val-store.opts", (String)"skip-cache"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object opts) {
        Object object;
        Object or__5581__auto__21083;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = opts;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = or__5581__auto__21083 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5581__auto__21083;
            or__5581__auto__21083 = null;
        } else {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = opts;
            opts = null;
            object = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object) {
                __thunk__1__ = __site__1__.fault(object5);
                object = __thunk__1__.get(object5);
            }
        }
        return iFn.invoke((Object)(RT.booleanCast((Object)object) ? Boolean.TRUE : Boolean.FALSE));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return double_store$get_from_near_store_QMARK_.invokeStatic(object2);
    }
}

