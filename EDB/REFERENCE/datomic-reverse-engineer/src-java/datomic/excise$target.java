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

public final class excise$target
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-id");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"excise"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"id"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2, Object spec) {
        Object object;
        Object or__5238__auto__14849;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = spec;
        spec = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object id = object3;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = id;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object object6 = or__5238__auto__14849 = object5;
        if (object6 != null && object6 != Boolean.FALSE) {
            object = or__5238__auto__14849;
            or__5238__auto__14849 = null;
        } else {
            Object object7 = db2;
            db2 = null;
            Object object8 = id;
            id = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object7, object8);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$target.invokeStatic(object3, object4);
    }
}

