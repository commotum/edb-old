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

public final class backup$create_restore_job$fn__20238
extends AFunction {
    Object lookup;
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"log-root-walker");

    public backup$create_restore_job$fn__20238(Object object) {
        this.lookup = object;
    }

    public Object invoke(Object p1__20236_SHARP_) {
        Object object = p1__20236_SHARP_;
        p1__20236_SHARP_ = null;
        backup$create_restore_job$fn__20238 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.lookup, (Object)Boolean.FALSE);
    }
}

