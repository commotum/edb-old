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

public final class tools$cr_QMARK_
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object o) {
        Object object;
        Object and__5236__auto__21773;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = o;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = and__5236__auto__21773 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = o;
            o = null;
            object = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object) {
                __thunk__1__ = __site__1__.fault(object5);
                object = __thunk__1__.get(object5);
            }
        } else {
            object = and__5236__auto__21773;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$cr_QMARK_.invokeStatic(object2);
    }
}

