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

public final class thread$pmap_n$fn__21038
extends AFunction {
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");

    public thread$pmap_n$fn__21038(Object object) {
        this.f = object;
    }

    public Object invoke(Object p1__21020_SHARP_) {
        Object object = p1__21020_SHARP_;
        p1__21020_SHARP_ = null;
        thread$pmap_n$fn__21038 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.f, object);
    }
}

