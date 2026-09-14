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
import datomic.log.Tail;
import java.util.Arrays;

public final class log$create_log_impl
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"tail-descriptor?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"tail-descriptor?"), Symbol.intern(null, (String)"desc")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Object const__6 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"instance?"), Symbol.intern(null, (String)"Tail"), Symbol.intern(null, (String)"tail")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"->LogImpl");

    public static Object invokeStatic(Object olookup, Object desc, Object tail) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(desc);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        if (!(tail instanceof Tail)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__6))));
        }
        Object object2 = olookup;
        olookup = null;
        Object object3 = desc;
        desc = null;
        Object object4 = tail;
        tail = null;
        return ((IFn)const__7.getRawRoot()).invoke(object2, object3, object4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$create_log_impl.invokeStatic(object4, object5, object6);
    }
}

