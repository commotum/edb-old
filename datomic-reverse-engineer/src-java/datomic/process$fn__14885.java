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

public final class process$fn__14885
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"commute");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*loaded-libs*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"datomic.process");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2), const__3.getRawRoot(), (Object)const__4);
    }

    public Object invoke() {
        return process$fn__14885.invokeStatic();
    }
}

