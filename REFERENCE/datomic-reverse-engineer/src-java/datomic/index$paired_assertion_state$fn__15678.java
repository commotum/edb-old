/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class index$paired_assertion_state$fn__15678
extends AFunction {
    Object e;
    Object v;
    Object a;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"item");
    public static final Var const__8 = RT.var((String)"datomic.common", (String)"compare");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public index$paired_assertion_state$fn__15678(Object object, Object object2, Object object3) {
        this.e = object;
        this.v = object2;
        this.a = object3;
    }

    public Object invoke(Object p__15677) {
        Boolean bl;
        Object map__15679;
        Object object;
        Object object2 = p__15677;
        p__15677 = null;
        Object map__156792 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__156792);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__156792;
            map__156792 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__156792;
            map__156792 = null;
        }
        Object object5 = map__15679 = object;
        map__15679 = null;
        Object item = RT.get((Object)object5, (Object)const__3);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = item;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        boolean and__5236__auto__15682 = Util.equiv((Object)object7, (Object)this_.e);
        if (and__5236__auto__15682) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object8 = item;
            Object object9 = iLookupThunk2.get(object8);
            if (iLookupThunk2 == object9) {
                __thunk__1__ = __site__1__.fault(object8);
                object9 = __thunk__1__.get(object8);
            }
            boolean and__5236__auto__15681 = Util.equiv((Object)object9, (Object)this_.a);
            if (and__5236__auto__15681) {
                IFn.OOL oOL = (IFn.OOL)const__8.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object10 = item;
                item = null;
                Object object11 = iLookupThunk3.get(object10);
                if (iLookupThunk3 == object11) {
                    __thunk__2__ = __site__2__.fault(object10);
                    object11 = __thunk__2__.get(object10);
                }
                index$paired_assertion_state$fn__15678 this_ = null;
                bl = Numbers.isZero((long)oOL.invokePrim(object11, this_.v)) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__15681 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__15682 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

