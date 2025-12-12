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
import datomic.log$write_excised_log$fn__16409$excise_QMARK___16416$fn__16417;

public final class log$write_excised_log$fn__16409$excise_QMARK___16416
extends AFunction {
    Object xpreds;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");

    public log$write_excised_log$fn__16409$excise_QMARK___16416(Object object) {
        this.xpreds = object;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        log$write_excised_log$fn__16409$excise_QMARK___16416 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new log$write_excised_log$fn__16409$excise_QMARK___16416$fn__16417(object), this_.xpreds);
    }
}

