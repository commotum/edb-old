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
import datomic.index$idx_key$fn__15551$fn__15552;

public final class index$idx_key$fn__15551
extends AFunction {
    Object ks;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");

    public index$idx_key$fn__15551(Object object) {
        this.ks = object;
    }

    public Object invoke(Object m) {
        Object object = m;
        m = null;
        index$idx_key$fn__15551 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new index$idx_key$fn__15551$fn__15552(object), this_.ks);
    }
}

