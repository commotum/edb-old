/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class db$fn__13610
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"datomic.builtins");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
    }

    public Object invoke() {
        return db$fn__13610.invokeStatic();
    }
}

