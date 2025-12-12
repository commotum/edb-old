/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.extension_resolver$wildcard_pred$fn__14307;

public final class extension_resolver$wildcard_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"datomic.extension-resolver", (String)"wildcard-name?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"namespace");

    public static Object invokeStatic(Object allow) {
        Object nses;
        Object object = allow;
        allow = null;
        Object object2 = nses = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot()), ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot())), object);
        nses = null;
        return new extension_resolver$wildcard_pred$fn__14307(object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$wildcard_pred.invokeStatic(object2);
    }
}

