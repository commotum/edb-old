/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.excise$create_as_pred$reify__14869$fn__14870;
import datomic.excise$create_as_pred$reify__14869$fn__14872;
import datomic.excise.ExcisePred;

public final class excise$create_as_pred$reify__14869
implements ExcisePred,
IObj {
    final IPersistentMap __meta;
    Object a__GT_xpreds;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public excise$create_as_pred$reify__14869(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.a__GT_xpreds = object;
    }

    public excise$create_as_pred$reify__14869(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new excise$create_as_pred$reify__14869(iPersistentMap, this.a__GT_xpreds);
    }

    public Object ep_remove_QMARK_(Object d) {
        Object object;
        Object temp__5457__auto__14875;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = d;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = temp__5457__auto__14875 = RT.get((Object)this_.a__GT_xpreds, (Object)object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__14875;
            temp__5457__auto__14875 = null;
            Object xpreds = object5;
            Object object6 = d;
            d = null;
            Object object7 = xpreds;
            xpreds = null;
            excise$create_as_pred$reify__14869 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)new excise$create_as_pred$reify__14869$fn__14872(object6), object7);
        } else {
            object = null;
        }
        return object;
    }

    public Object ep_datoms() {
        excise$create_as_pred$reify__14869 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new excise$create_as_pred$reify__14869$fn__14870(), ((IFn)const__1.getRawRoot()).invoke(this_.a__GT_xpreds));
    }
}

