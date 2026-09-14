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
import datomic.db$valid_tuple_assert_QMARK_$fn__13831;
import datomic.db$valid_tuple_assert_QMARK_$fn__13833;
import datomic.db$valid_tuple_assert_QMARK_$valid_QMARK___13825;

public final class db$valid_tuple_assert_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vector?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"<=");
    public static final Object const__7 = 2L;
    public static final Object const__8 = 8L;
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"true?");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"map");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleAttrs"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleAttrs"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleType"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleType"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleTypes"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleTypes"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object attr, Object tup) {
        Object object;
        db$valid_tuple_assert_QMARK_$valid_QMARK___13825 valid_QMARK_ = new db$valid_tuple_assert_QMARK_$valid_QMARK___13825();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = attr;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object and__5236__auto__13836;
            Object object4 = and__5236__auto__13836 = ((IFn)const__1.getRawRoot()).invoke(tup);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = tup;
                tup = null;
                long l = RT.count((Object)object5);
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object6 = attr;
                attr = null;
                Object object7 = iLookupThunk2.get(object6);
                if (iLookupThunk2 == object7) {
                    __thunk__1__ = __site__1__.fault(object6);
                    object7 = __thunk__1__.get(object6);
                }
                object = Util.equiv((long)l, (long)RT.count((Object)object7)) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object = and__5236__auto__13836;
                and__5236__auto__13836 = null;
            }
        } else {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object8 = attr;
            Object object9 = iLookupThunk3.get(object8);
            if (iLookupThunk3 == object9) {
                __thunk__2__ = __site__2__.fault(object8);
                object9 = __thunk__2__.get(object8);
            }
            if (object9 != null && object9 != Boolean.FALSE) {
                Object and__5236__auto__13838;
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object10 = attr;
                attr = null;
                Object object11 = iLookupThunk4.get(object10);
                if (iLookupThunk4 == object11) {
                    __thunk__3__ = __site__3__.fault(object10);
                    object11 = __thunk__3__.get(object10);
                }
                Object kw = object11;
                Object object12 = and__5236__auto__13838 = ((IFn)const__1.getRawRoot()).invoke(tup);
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object and__5236__auto__13837;
                    db$valid_tuple_assert_QMARK_$valid_QMARK___13825 db$valid_tuple_assert_QMARK_$valid_QMARK___13825 = valid_QMARK_;
                    valid_QMARK_ = null;
                    Object object13 = kw;
                    kw = null;
                    Object object14 = and__5236__auto__13837 = ((IFn)const__5.getRawRoot()).invoke((Object)new db$valid_tuple_assert_QMARK_$fn__13831((Object)db$valid_tuple_assert_QMARK_$valid_QMARK___13825, object13), tup);
                    if (object14 != null && object14 != Boolean.FALSE) {
                        Object object15 = tup;
                        tup = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(const__7, (Object)RT.count((Object)object15), const__8);
                    } else {
                        object = and__5236__auto__13837;
                        and__5236__auto__13837 = null;
                    }
                } else {
                    object = and__5236__auto__13838;
                    and__5236__auto__13838 = null;
                }
            } else {
                ILookupThunk iLookupThunk5 = __thunk__4__;
                Object object16 = attr;
                Object object17 = iLookupThunk5.get(object16);
                if (iLookupThunk5 == object17) {
                    __thunk__4__ = __site__4__.fault(object16);
                    object17 = __thunk__4__.get(object16);
                }
                if (object17 != null && object17 != Boolean.FALSE) {
                    Object and__5236__auto__13840;
                    IFn iFn = (IFn)const__10.getRawRoot();
                    ILookupThunk iLookupThunk6 = __thunk__5__;
                    Object object18 = attr;
                    attr = null;
                    Object object19 = iLookupThunk6.get(object18);
                    if (iLookupThunk6 == object19) {
                        __thunk__5__ = __site__5__.fault(object18);
                        object19 = __thunk__5__.get(object18);
                    }
                    Object kws = iFn.invoke(object19);
                    Object object20 = and__5236__auto__13840 = ((IFn)const__1.getRawRoot()).invoke(tup);
                    if (object20 != null && object20 != Boolean.FALSE) {
                        boolean and__5236__auto__13839 = Util.equiv((long)RT.count((Object)kws), (long)RT.count((Object)tup));
                        if (and__5236__auto__13839) {
                            db$valid_tuple_assert_QMARK_$valid_QMARK___13825 db$valid_tuple_assert_QMARK_$valid_QMARK___13825 = valid_QMARK_;
                            valid_QMARK_ = null;
                            Object object21 = kws;
                            kws = null;
                            Object object22 = tup;
                            tup = null;
                            object = ((IFn)const__5.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__12.getRawRoot()).invoke((Object)new db$valid_tuple_assert_QMARK_$fn__13833((Object)db$valid_tuple_assert_QMARK_$valid_QMARK___13825), object21, object22));
                        } else {
                            object = and__5236__auto__13839 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object = and__5236__auto__13840;
                        and__5236__auto__13840 = null;
                    }
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$valid_tuple_assert_QMARK_.invokeStatic(object3, object4);
    }
}

