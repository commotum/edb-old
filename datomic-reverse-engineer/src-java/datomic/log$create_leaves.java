/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.KeywordLookupSite
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
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class log$create_leaves
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"resets-caches?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__5 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), Symbol.intern(null, (String)"resets-caches?"), ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"bufs"), Symbol.intern(null, (String)"tail")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 34}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"create-leaves*");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__9 = RT.var((String)"datomic.log", (String)"tail-ts");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"bufs"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"bufs"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object target_size, Object tail) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = tail;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = iFn.invoke(object, object3);
        if (object4 == null || object4 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__4.getRawRoot()).invoke(const__5))));
        }
        IFn iFn2 = (IFn)const__6.getRawRoot();
        Object object5 = target_size;
        target_size = null;
        IFn iFn3 = (IFn)const__7.getRawRoot();
        Object object6 = const__8.getRawRoot();
        Object object7 = ((IFn)const__9.getRawRoot()).invoke(tail);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object8 = tail;
        tail = null;
        Object object9 = iLookupThunk2.get(object8);
        if (iLookupThunk2 == object9) {
            __thunk__1__ = __site__1__.fault(object8);
            object9 = __thunk__1__.get(object8);
        }
        return iFn2.invoke(object5, iFn3.invoke(object6, object7, object9));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$create_leaves.invokeStatic(object3, object4);
    }
}

