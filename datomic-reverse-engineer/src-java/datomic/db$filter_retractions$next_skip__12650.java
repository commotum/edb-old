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

public final class db$filter_retractions$next_skip__12650
extends AFunction {
    Object skip;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"inext");

    public db$filter_retractions$next_skip__12650(Object object) {
        this.skip = object;
    }

    public Object invoke(Object p1__12640_SHARP_) {
        Object object = p1__12640_SHARP_;
        p1__12640_SHARP_ = null;
        db$filter_retractions$next_skip__12650 this_ = null;
        return ((IFn)this_.skip).invoke(((IFn)const__0.getRawRoot()).invoke(object));
    }
}

