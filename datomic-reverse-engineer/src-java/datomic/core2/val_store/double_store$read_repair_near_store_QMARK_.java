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

public final class double_store$read_repair_near_store_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"datomic.core2.val-store.opts", (String)"skip-cache"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object opts) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = opts;
        opts = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return iFn.invoke((Object)(RT.booleanCast((Object)object2) ? Boolean.TRUE : Boolean.FALSE));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return double_store$read_repair_near_store_QMARK_.invokeStatic(object2);
    }
}

