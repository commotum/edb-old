/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.ThreadPoolExecutor;

public final class thread$fn__21047$fn__21048
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"class");

    public Object invoke(Object pool, Object _) {
        Object object = pool;
        pool = null;
        thread$fn__21047$fn__21048 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((ThreadPoolExecutor)object).getQueue());
    }
}

