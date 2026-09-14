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

public final class index$capture_last_ex$ex_handler__15426
extends AFunction {
    Object last_ex;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");

    public index$capture_last_ex$ex_handler__15426(Object object) {
        this.last_ex = object;
    }

    public Object invoke(Object t) {
        Object object = t;
        t = null;
        ((IFn)const__0.getRawRoot()).invoke(this.last_ex, object);
        return null;
    }
}

