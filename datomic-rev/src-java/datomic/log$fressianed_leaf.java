/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class log$fressianed_leaf
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vector?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"vector?"), Symbol.intern(null, (String)"val")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Object const__7 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"not"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"zero?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"val")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 23}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 16}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Object const__10 = ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"id"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), Symbol.intern(null, (String)"val")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 16}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Object const__12 = ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"data"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), Symbol.intern(null, (String)"val")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 18}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__13 = RT.var((String)"datomic.fressian", (String)"byte-buf");
    public static final Keyword const__14 = RT.keyword(null, (String)"handlers");
    public static final Var const__15 = RT.var((String)"datomic.log", (String)"write-handlers");
    public static final Keyword const__16 = RT.keyword(null, (String)"footer");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object val) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(val);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        Object object2 = ((IFn)const__4.getRawRoot()).invoke((Object)(Numbers.isZero((long)RT.count((Object)val)) ? Boolean.TRUE : Boolean.FALSE));
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__7))));
        }
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = ((IFn)const__9.getRawRoot()).invoke(val);
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (object4 == null || object4 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__10))));
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = ((IFn)const__9.getRawRoot()).invoke(val);
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__12))));
        }
        Object object7 = val;
        val = null;
        return ((IFn)const__13.getRawRoot()).invoke(object7, (Object)const__14, const__15.getRawRoot(), (Object)const__16, (Object)Boolean.TRUE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fressianed_leaf.invokeStatic(object2);
    }
}

