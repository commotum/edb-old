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
import java.io.FileNotFoundException;

public final class require$maybe_require$fn__638
extends AFunction {
    Object s;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"require");

    public require$maybe_require$fn__638(Object object) {
        this.s = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.s);
        }
        catch (FileNotFoundException _) {
            object = null;
        }
        return object;
    }
}

