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

public final class btset$bench$fn__11877
extends AFunction {
    Object bt;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public btset$bench$fn__11877(Object object) {
        this.bt = object;
    }

    public Object invoke(Object p1__11864_SHARP_) {
        Object object = p1__11864_SHARP_;
        p1__11864_SHARP_ = null;
        btset$bench$fn__11877 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.bt, object);
    }
}

