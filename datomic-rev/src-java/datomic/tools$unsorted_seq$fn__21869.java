/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;

public final class tools$unsorted_seq$fn__21869
extends AFunction {
    Object pred;

    public tools$unsorted_seq$fn__21869(Object object) {
        this.pred = object;
    }

    public Object invoke(Object p__21868) {
        Object object = p__21868;
        p__21868 = null;
        Object vec__21870 = object;
        Object a = RT.nth((Object)vec__21870, (int)RT.intCast((long)0L), null);
        Object object2 = vec__21870;
        vec__21870 = null;
        Object b = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = a;
        a = null;
        Object object4 = b;
        b = null;
        tools$unsorted_seq$fn__21869 this_ = null;
        return ((IFn)this_.pred).invoke(object3, object4);
    }
}

