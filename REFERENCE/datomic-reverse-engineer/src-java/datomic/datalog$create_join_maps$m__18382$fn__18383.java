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

public final class datalog$create_join_maps$m__18382$fn__18383
extends AFunction {
    Object ib;
    Object jb;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");

    public datalog$create_join_maps$m__18382$fn__18383(Object object, Object object2) {
        this.ib = object;
        this.jb = object2;
    }

    public Object invoke(Object p1__18381_SHARP_, Object p2__18380_SHARP_) {
        Object object;
        Object object2 = ((IFn)this_.ib).invoke(p2__18380_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__18381_SHARP_;
            p1__18381_SHARP_ = null;
            Object object4 = ((IFn)this_.ib).invoke(p2__18380_SHARP_);
            Object object5 = p2__18380_SHARP_;
            p2__18380_SHARP_ = null;
            datalog$create_join_maps$m__18382$fn__18383 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object3, object4, ((IFn)this_.jb).invoke(object5));
        } else {
            object = p1__18381_SHARP_;
            Object var1_1 = null;
        }
        return object;
    }
}

