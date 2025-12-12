/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$sparse_e_xf$fn__15398$fn__15399
extends AFunction {
    Object vprev;
    Object olookup;
    Object idx;
    Object rf;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vreset!");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    public static final Var const__4 = RT.var((String)"datomic.index", (String)"seg-last-datom");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"datomic.index", (String)"sparse-datom");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"last-d"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"last-d"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"last-d"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"segid"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public index$sparse_e_xf$fn__15398$fn__15399(Object object, Object object2, Object object3, Object object4) {
        this.vprev = object;
        this.olookup = object2;
        this.idx = object3;
        this.rf = object4;
    }

    public Object invoke(Object result2, Object entry) {
        Object object;
        index$sparse_e_xf$fn__15398$fn__15399 this_;
        Object object2;
        Object and__5236__auto__15402;
        Object prev_entry = ((IFn)const__0.getRawRoot()).invoke(this_.vprev);
        ((IFn)const__1.getRawRoot()).invoke(this_.vprev, entry);
        Object object3 = and__5236__auto__15402 = prev_entry;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object or__5238__auto__15401;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = entry;
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__0__ = __site__0__.fault(object4);
                object5 = __thunk__0__.get(object4);
            }
            Object object6 = or__5238__auto__15401 = object5;
            if (object6 != null && object6 != Boolean.FALSE) {
                object2 = or__5238__auto__15401;
                or__5238__auto__15401 = null;
            } else {
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object7 = prev_entry;
                object2 = iLookupThunk2.get(object7);
                if (iLookupThunk2 == object2) {
                    __thunk__1__ = __site__1__.fault(object7);
                    object2 = __thunk__1__.get(object7);
                }
            }
        } else {
            object2 = and__5236__auto__15402;
            and__5236__auto__15402 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object8;
            Object or__5238__auto__15403;
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object9 = entry;
            Object object10 = iLookupThunk.get(object9);
            if (iLookupThunk == object10) {
                __thunk__2__ = __site__2__.fault(object9);
                object10 = __thunk__2__.get(object9);
            }
            Object d = object10;
            ILookupThunk iLookupThunk3 = __thunk__3__;
            Object object11 = prev_entry;
            Object object12 = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object12) {
                __thunk__3__ = __site__3__.fault(object11);
                object12 = __thunk__3__.get(object11);
            }
            Object object13 = or__5238__auto__15403 = object12;
            if (object13 != null && object13 != Boolean.FALSE) {
                object8 = or__5238__auto__15403;
                or__5238__auto__15403 = null;
            } else {
                IFn iFn = (IFn)const__4.getRawRoot();
                ILookupThunk iLookupThunk4 = __thunk__4__;
                Object object14 = prev_entry;
                prev_entry = null;
                Object object15 = iLookupThunk4.get(object14);
                if (iLookupThunk4 == object15) {
                    __thunk__4__ = __site__4__.fault(object14);
                    object15 = __thunk__4__.get(object14);
                }
                object8 = iFn.invoke(this_.olookup, object15);
            }
            Object prior = object8;
            Object object16 = result2;
            result2 = null;
            Object object17 = entry;
            entry = null;
            Object object18 = prior;
            prior = null;
            Object object19 = d;
            d = null;
            this_ = null;
            object = ((IFn)this_.rf).invoke(object16, ((IFn)const__6.getRawRoot()).invoke(object17, (Object)const__3, ((IFn)const__7.getRawRoot()).invoke(this_.idx, object18, object19)));
        } else {
            Object object20 = result2;
            result2 = null;
            Object object21 = entry;
            entry = null;
            this_ = null;
            object = ((IFn)this_.rf).invoke(object20, object21);
        }
        return object;
    }

    public Object invoke(Object result2) {
        Object object = result2;
        result2 = null;
        index$sparse_e_xf$fn__15398$fn__15399 this_ = null;
        return ((IFn)this_.rf).invoke(object);
    }

    public Object invoke() {
        index$sparse_e_xf$fn__15398$fn__15399 this_ = null;
        return ((IFn)this_.rf).invoke();
    }
}

