/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Var;

public final class assert$assertion_repl$fn__20656
extends AFunction {
    Object error;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"*e");

    public assert$assertion_repl$fn__20656(Object object) {
        this.error = object;
    }

    public Object invoke() {
        return const__0.set(this.error);
    }
}

