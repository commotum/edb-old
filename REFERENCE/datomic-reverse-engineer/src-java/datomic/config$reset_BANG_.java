/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.config$reset_BANG_$fn__840;

public final class config$reset_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"properties-ref");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"warn-deprecations");
    public static final Var const__3 = RT.var((String)"datomic.config", (String)"validate-properties");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"datomic.config", (String)"config-table");

    public static Object invokeStatic() {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        ((IFn)const__2.getRawRoot()).invoke();
        return iFn.invoke(object, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)new config$reset_BANG_$fn__840(), (Object)PersistentArrayMap.EMPTY, const__5.getRawRoot())));
    }

    public Object invoke() {
        return config$reset_BANG_.invokeStatic();
    }
}

