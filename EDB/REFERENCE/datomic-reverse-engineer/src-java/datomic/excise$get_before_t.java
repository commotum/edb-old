/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class excise$get_before_t
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"as-of-t");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db.excise", (String)"beforeT"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db.excise", (String)"before"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2, Object spec) {
        Object object;
        Object before;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = spec;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object before_t = object3;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = spec;
        spec = null;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object object6 = before = object5;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7;
            Object or__5238__auto__14810;
            Object object8 = before_t;
            before_t = null;
            Object object9 = or__5238__auto__14810 = object8;
            if (object9 != null && object9 != Boolean.FALSE) {
                object7 = or__5238__auto__14810;
                or__5238__auto__14810 = null;
            } else {
                object7 = Numbers.num((long)Long.MAX_VALUE);
            }
            Object object10 = db2;
            db2 = null;
            Object object11 = before;
            before = null;
            object = Numbers.min((Object)object7, (Object)((IFn)const__3.getRawRoot()).invoke(object10, object11));
        } else {
            object = before_t;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$get_before_t.invokeStatic(object3, object4);
    }
}

