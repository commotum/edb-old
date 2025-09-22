/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class log$tail_descriptor_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"string?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"l"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"etag"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"etag"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object desc) {
        Object object;
        Object and__5236__auto__16178;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = desc;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = and__5236__auto__16178 = iFn.invoke(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object and__5236__auto__16177;
            IFn iFn2 = (IFn)const__0.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = desc;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            Object object7 = and__5236__auto__16177 = iFn2.invoke(object6);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object and__5236__auto__16176;
                Object object8;
                Object or__5238__auto__16175;
                IFn iFn3 = (IFn)const__3.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object9 = desc;
                Object object10 = iLookupThunk3.get(object9);
                if (iLookupThunk3 == object10) {
                    __thunk__2__ = __site__2__.fault(object9);
                    object10 = __thunk__2__.get(object9);
                }
                Object object11 = or__5238__auto__16175 = iFn3.invoke(object10);
                if (object11 != null && object11 != Boolean.FALSE) {
                    object8 = or__5238__auto__16175;
                    or__5238__auto__16175 = null;
                } else {
                    ILookupThunk iLookupThunk4 = __thunk__3__;
                    Object object12 = desc;
                    Object object13 = iLookupThunk4.get(object12);
                    if (iLookupThunk4 == object13) {
                        __thunk__3__ = __site__3__.fault(object12);
                        object13 = __thunk__3__.get(object12);
                    }
                    object8 = Util.identical((Object)object13, null) ? Boolean.TRUE : Boolean.FALSE;
                }
                Object object14 = and__5236__auto__16176 = object8;
                if (object14 != null && object14 != Boolean.FALSE) {
                    IFn iFn4 = (IFn)const__3.getRawRoot();
                    ILookupThunk iLookupThunk5 = __thunk__4__;
                    Object object15 = desc;
                    desc = null;
                    Object object16 = iLookupThunk5.get(object15);
                    if (iLookupThunk5 == object16) {
                        __thunk__4__ = __site__4__.fault(object15);
                        object16 = __thunk__4__.get(object15);
                    }
                    object = iFn4.invoke(object16);
                } else {
                    object = and__5236__auto__16176;
                    Object var3_3 = null;
                }
            } else {
                object = and__5236__auto__16177;
                Object var2_2 = null;
            }
        } else {
            object = and__5236__auto__16178;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$tail_descriptor_QMARK_.invokeStatic(object2);
    }
}

