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

public final class db$set_element_fields$fn__13148
extends AFunction {
    Object kvs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");

    public db$set_element_fields$fn__13148(Object object) {
        this.kvs = object;
    }

    public Object invoke(Object p1__13147_SHARP_) {
        Object object = p1__13147_SHARP_;
        p1__13147_SHARP_ = null;
        db$set_element_fields$fn__13148 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object, this_.kvs);
    }
}

