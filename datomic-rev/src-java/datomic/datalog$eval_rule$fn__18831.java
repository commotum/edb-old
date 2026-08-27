/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class datalog$eval_rule$fn__18831
extends AFunction {
    Object adorn;

    public datalog$eval_rule$fn__18831(Object object) {
        this.adorn = object;
    }

    public Object invoke(Object i, Object a) {
        Object object;
        Object object2 = i;
        i = null;
        Object object3 = ((IFn)this.adorn).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = null;
        } else {
            object = a;
            Object var2_2 = null;
        }
        return object;
    }
}

