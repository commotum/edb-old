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

public final class excise$pred_and_extent$id__14815
extends AFunction {
    Object db;
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"resolve-id");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public excise$pred_and_extent$id__14815(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__14811_SHARP_) {
        Object object;
        Object or__5238__auto__14817;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = p1__14811_SHARP_;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = or__5238__auto__14817 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__14817;
            or__5238__auto__14817 = null;
        } else {
            Object object5 = p1__14811_SHARP_;
            p1__14811_SHARP_ = null;
            excise$pred_and_extent$id__14815 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(this_.db, object5);
        }
        return object;
    }
}

