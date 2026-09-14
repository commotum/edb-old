/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Log;

public final class extensions$tx_data
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object log2, Object t) {
        Object object;
        Object or__5238__auto__18010;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = log2;
        log2 = null;
        Object object3 = t;
        Object object4 = t;
        t = null;
        Object object5 = ((IFn)const__1.getRawRoot()).invoke(((Log)object2).txRange(object3, Numbers.inc((Object)object4)));
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        Object object7 = or__5238__auto__18010 = object6;
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__18010;
            or__5238__auto__18010 = null;
        } else {
            object = PersistentVector.EMPTY;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extensions$tx_data.invokeStatic(object3, object4);
    }
}

