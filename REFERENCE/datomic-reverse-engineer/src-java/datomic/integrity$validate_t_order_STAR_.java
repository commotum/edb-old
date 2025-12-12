/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$validate_t_order_STAR_$fn__22233;
import java.util.Arrays;

public final class integrity$validate_t_order_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__2 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Object const__3 = 0L;
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"first");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), 1000L, ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"t"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"seq"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"iter", (String)"iter-seq"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"log", (String)"seek-tx"), Symbol.intern(null, (String)"log"), 0L))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 54}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 39}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 34}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 27}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 23}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15}));
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__10 = RT.keyword(null, (String)"bindings");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"log");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"progress");
    public static final Keyword const__13 = RT.keyword(null, (String)"form");
    public static final Var const__14 = RT.var((String)"datomic.assert", (String)"*assert-handler*");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"partition-all");
    public static final Object const__17 = 2L;
    public static final Object const__18 = 1L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object log2, Object progress) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(log2, const__3)));
        if (object2 != null && object2 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(log2, const__3))));
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            if (Util.equiv((long)1000L, (Object)object4)) {
            } else {
                Object form__20659__auto__22253 = const__8;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__10;
                objectArray[1] = RT.mapUniqueKeys((Object[])new Object[]{const__11, log2, const__12, progress});
                objectArray[2] = const__13;
                Object object5 = form__20659__auto__22253;
                form__20659__auto__22253 = null;
                objectArray[3] = object5;
                Object error__20660__auto__22254 = ((IFn)const__9.getRawRoot()).invoke((Object)"Assertion failed, see ex-data for details", (Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object6 = const__14.get();
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = error__20660__auto__22254;
                    error__20660__auto__22254 = null;
                    ((IFn)const__14.get()).invoke(object7);
                } else {
                    Object object8 = error__20660__auto__22254;
                    error__20660__auto__22254 = null;
                    throw (Throwable)object8;
                }
            }
            Object object9 = progress;
            progress = null;
            integrity$validate_t_order_STAR_$fn__22233 integrity$validate_t_order_STAR_$fn__22233 = new integrity$validate_t_order_STAR_$fn__22233(log2, object9);
            Object object10 = log2;
            log2 = null;
            object = ((IFn)const__15.getRawRoot()).invoke((Object)integrity$validate_t_order_STAR_$fn__22233, const__3, ((IFn)const__16.getRawRoot()).invoke(const__17, const__18, ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object10, const__3))));
        } else {
            object = const__3;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$validate_t_order_STAR_.invokeStatic(object3, object4);
    }
}

