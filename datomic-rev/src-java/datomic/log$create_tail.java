/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class log$create_tail
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"txes")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 14})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"bufs")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 27}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"resets-caches?");
    public static final Object const__7 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), Symbol.intern(null, (String)"resets-caches?"), Symbol.intern(null, (String)"bufs")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__8 = RT.var((String)"datomic.log", (String)"->Tail");

    public static Object invokeStatic(Object txes, Object bufs) {
        if ((long)RT.count((Object)txes) != (long)RT.count((Object)bufs)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        }
        Object object = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), bufs);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__7))));
        }
        Object object2 = txes;
        txes = null;
        Object object3 = bufs;
        bufs = null;
        return ((IFn)const__8.getRawRoot()).invoke(object2, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$create_tail.invokeStatic(object3, object4);
    }
}

