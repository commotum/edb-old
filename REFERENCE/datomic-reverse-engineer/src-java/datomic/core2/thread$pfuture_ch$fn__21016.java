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

public final class thread$pfuture_ch$fn__21016
extends AFunction {
    Object f;
    Object ch;
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"put!");

    public thread$pfuture_ch$fn__21016(Object object, Object object2) {
        this.f = object;
        this.ch = object2;
    }

    public Object invoke() {
        thread$pfuture_ch$fn__21016 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.ch, ((IFn)this_.f).invoke());
    }
}

