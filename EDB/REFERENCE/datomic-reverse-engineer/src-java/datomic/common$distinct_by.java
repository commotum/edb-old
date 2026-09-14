/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import datomic.common$distinct_by$fn__9243;
import datomic.common$distinct_by$step__9247;

public final class common$distinct_by
extends AFunction {
    public static Object invokeStatic(Object f, Object coll) {
        common$distinct_by$step__9247 step;
        Object object = f;
        f = null;
        common$distinct_by$step__9247 common$distinct_by$step__9247 = step = new common$distinct_by$step__9247(object);
        step = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)common$distinct_by$step__9247).invoke(object2, (Object)PersistentHashSet.EMPTY);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$distinct_by.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return new common$distinct_by$fn__9243(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$distinct_by.invokeStatic(object2);
    }
}

