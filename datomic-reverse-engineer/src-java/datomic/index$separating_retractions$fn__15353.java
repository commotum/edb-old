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

public final class index$separating_retractions$fn__15353
extends AFunction {
    Object nd;
    Object retref;
    Object db;
    Object d;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"separating-retractions");

    public index$separating_retractions$fn__15353(Object object, Object object2, Object object3, Object object4) {
        this.nd = object;
        this.retref = object2;
        this.db = object3;
        this.d = object4;
    }

    public Object invoke() {
        index$separating_retractions$fn__15353 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.d, ((IFn)const__1.getRawRoot()).invoke(this_.db, this_.retref, this_.nd));
    }
}

