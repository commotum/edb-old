/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$partition_by_weight$fn__16529$fn__16530;

public final class log$partition_by_weight$fn__16529
extends AFunction {
    Object target;
    Object weigh;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"volatile!");
    public static final Object const__1 = 0L;

    public log$partition_by_weight$fn__16529(Object object, Object object2) {
        this.target = object;
        this.weigh = object2;
    }

    public Object invoke(Object rf) {
        Object weight_ref = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object part_ref = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        Object object = rf;
        rf = null;
        Object object2 = weight_ref;
        weight_ref = null;
        Object object3 = part_ref;
        part_ref = null;
        return new log$partition_by_weight$fn__16529$fn__16530(this.target, object, this.weigh, object2, object3);
    }
}

