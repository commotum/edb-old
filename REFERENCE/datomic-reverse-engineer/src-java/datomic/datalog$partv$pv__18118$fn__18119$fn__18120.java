/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Iterator;

public final class datalog$partv$pv__18118$fn__18119$fn__18120
extends AFunction {
    Object iter;
    long n;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj!");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"persistent!");

    public datalog$partv$pv__18118$fn__18119$fn__18120(Object object, long l) {
        this.iter = object;
        this.n = l;
    }

    public Object invoke() {
        boolean and__5236__auto__18122;
        long i = 0L;
        Object ret = ((IFn)const__1.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        while ((and__5236__auto__18122 = Numbers.lt((long)i, (long)this_.n)) ? ((Iterator)this_.iter).hasNext() : and__5236__auto__18122) {
            Object object = ret;
            ret = null;
            ret = ((IFn)const__4.getRawRoot()).invoke(object, ((Iterator)this_.iter).next());
            ++i;
        }
        Object object = ret;
        ret = null;
        datalog$partv$pv__18118$fn__18119$fn__18120 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(object);
    }
}

