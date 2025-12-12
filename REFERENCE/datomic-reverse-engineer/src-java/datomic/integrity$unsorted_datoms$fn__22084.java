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
import java.util.Comparator;

public final class integrity$unsorted_datoms$fn__22084
extends AFunction {
    Object progress;
    Object op;
    Object cmp;
    public static final Object const__0 = 0L;

    public integrity$unsorted_datoms$fn__22084(Object object, Object object2, Object object3) {
        this.progress = object;
        this.op = object2;
        this.cmp = object3;
    }

    public Object invoke(Object a, Object b) {
        ((IFn)this_.progress).invoke();
        Object object = a;
        a = null;
        Object object2 = b;
        b = null;
        integrity$unsorted_datoms$fn__22084 this_ = null;
        return ((IFn)this_.op).invoke((Object)((Comparator)this_.cmp).compare(object, object2), const__0);
    }
}

