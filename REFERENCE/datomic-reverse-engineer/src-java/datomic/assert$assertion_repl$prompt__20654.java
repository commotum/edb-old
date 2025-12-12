/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class assert$assertion_repl$prompt__20654
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"datomic.assert", (String)"*level*");

    public Object invoke() {
        assert$assertion_repl$prompt__20654 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"(", const__2.get(), (Object)")=> "));
    }
}

