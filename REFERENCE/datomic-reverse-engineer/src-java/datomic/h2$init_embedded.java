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
import datomic.h2$init_embedded$fn__11637;
import java.util.Arrays;

public final class h2$init_embedded
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__2 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"Class", (String)"forName"), "org.h2.Driver"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__3 = RT.var((String)"datomic.h2", (String)"ensure-admin-conn");

    public static Object invokeStatic(Object spec) {
        Object conn;
        Object or__5238__auto__11640;
        Class<?> clazz = Class.forName("org.h2.Driver");
        if (clazz == null || clazz == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke(const__2))));
        }
        Object object = or__5238__auto__11640 = ((IFn)const__3.getRawRoot()).invoke(spec);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)new RuntimeException("Unable to connect to embedded storage, make sure storage-admin-password is correct.");
        }
        Object object2 = or__5238__auto__11640;
        or__5238__auto__11640 = null;
        Object object3 = conn = object2;
        conn = null;
        ((IFn)new h2$init_embedded$fn__11637(object3)).invoke();
        Object object4 = null;
        return spec;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$init_embedded.invokeStatic(object2);
    }
}

