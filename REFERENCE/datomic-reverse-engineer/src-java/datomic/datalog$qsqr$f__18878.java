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

public final class datalog$qsqr$f__18878
extends AFunction {
    Object cancel;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");

    public datalog$qsqr$f__18878(Object object) {
        this.cancel = object;
    }

    public Object invoke() {
        datalog$qsqr$f__18878 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cancel, (Object)"timeout elapsed");
    }
}

