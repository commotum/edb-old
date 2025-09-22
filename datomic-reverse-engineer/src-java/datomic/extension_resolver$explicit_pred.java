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
import datomic.extension_resolver$explicit_pred$fn__14303;

public final class extension_resolver$explicit_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__2 = RT.var((String)"datomic.extension-resolver", (String)"wildcard-name?");

    public static Object invokeStatic(Object allow) {
        Object syms;
        Object object = allow;
        allow = null;
        Object object2 = syms = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), object);
        syms = null;
        return new extension_resolver$explicit_pred$fn__14303(object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$explicit_pred.invokeStatic(object2);
    }
}

