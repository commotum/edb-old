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

public final class log$write_excised_log$fn__16409$excise_QMARK___16416$fn__16417
extends AFunction {
    Object d;
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"remove?");

    public log$write_excised_log$fn__16409$excise_QMARK___16416$fn__16417(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__16407_SHARP_) {
        Object object = p1__16407_SHARP_;
        p1__16407_SHARP_ = null;
        log$write_excised_log$fn__16409$excise_QMARK___16416$fn__16417 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.d);
    }
}

