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

public final class backup$create_backup_job$fn__20233
extends AFunction {
    Object lookup;
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"log-root-walker");

    public backup$create_backup_job$fn__20233(Object object) {
        this.lookup = object;
    }

    public Object invoke(Object p1__20229_SHARP_) {
        Object object = p1__20229_SHARP_;
        p1__20229_SHARP_ = null;
        backup$create_backup_job$fn__20233 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.lookup, (Object)Boolean.FALSE);
    }
}

