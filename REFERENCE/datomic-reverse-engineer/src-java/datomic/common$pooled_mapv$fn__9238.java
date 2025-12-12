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
import datomic.common$pooled_mapv$fn__9238$fn__9239;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public final class common$pooled_mapv$fn__9238
extends AFunction {
    Object f;
    Object exec;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"bound-fn*");

    public common$pooled_mapv$fn__9238(Object object, Object object2) {
        this.f = object;
        this.exec = object2;
    }

    public Object invoke(Object p1__9237_SHARP_) {
        Object object = p1__9237_SHARP_;
        p1__9237_SHARP_ = null;
        return ((ExecutorService)this.exec).submit((Callable)((IFn)const__0.getRawRoot()).invoke((Object)new common$pooled_mapv$fn__9238$fn__9239(this.f, object)));
    }
}

