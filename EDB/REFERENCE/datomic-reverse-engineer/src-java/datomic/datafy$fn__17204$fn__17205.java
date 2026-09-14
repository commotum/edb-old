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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datafy$fn__17204$fn__17205
extends AFunction {
    Object c;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__2 = RT.keyword(null, (String)"static");
    public static final Keyword const__3 = RT.keyword(null, (String)"public");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"resolve");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"flags"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"flags"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"type"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"type"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public datafy$fn__17204$fn__17205(Object object) {
        this.c = object;
    }

    public Object invoke(Object p1__17203_SHARP_) {
        Object object;
        Object and__5236__auto__17209;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = p1__17203_SHARP_;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = and__5236__auto__17209 = iFn.invoke(object3, (Object)const__2);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object and__5236__auto__17208;
            IFn iFn2 = (IFn)const__0.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = p1__17203_SHARP_;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            Object object7 = and__5236__auto__17208 = iFn2.invoke(object6, (Object)const__3);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object and__5236__auto__17207;
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object8 = p1__17203_SHARP_;
                Object object9 = iLookupThunk3.get(object8);
                if (iLookupThunk3 == object9) {
                    __thunk__2__ = __site__2__.fault(object8);
                    object9 = __thunk__2__.get(object8);
                }
                Object object10 = and__5236__auto__17207 = object9;
                if (object10 != null && object10 != Boolean.FALSE) {
                    IFn iFn3 = (IFn)const__6.getRawRoot();
                    ILookupThunk iLookupThunk4 = __thunk__3__;
                    Object object11 = p1__17203_SHARP_;
                    p1__17203_SHARP_ = null;
                    Object object12 = iLookupThunk4.get(object11);
                    if (iLookupThunk4 == object12) {
                        __thunk__3__ = __site__3__.fault(object11);
                        object12 = __thunk__3__.get(object11);
                    }
                    datafy$fn__17204$fn__17205 this_ = null;
                    object = Util.equiv((Object)iFn3.invoke(object12), (Object)this_.c) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object = and__5236__auto__17207;
                    and__5236__auto__17207 = null;
                }
            } else {
                object = and__5236__auto__17208;
                Object var3_3 = null;
            }
        } else {
            object = and__5236__auto__17209;
            Object var2_2 = null;
        }
        return object;
    }
}

