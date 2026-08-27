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

public final class log$write_new_log$fn__16345
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"rand-uuid");

    public Object invoke() {
        log$write_new_log$fn__16345 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke();
    }
}

