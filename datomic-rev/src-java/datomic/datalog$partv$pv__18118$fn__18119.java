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
import datomic.datalog$partv$pv__18118$fn__18119$fn__18120;
import java.util.Iterator;

public final class datalog$partv$pv__18118$fn__18119
extends AFunction {
    Object iter;
    Object pv;
    long n;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");

    public datalog$partv$pv__18118$fn__18119(Object object, Object object2, long l) {
        this.iter = object;
        this.pv = object2;
        this.n = l;
    }

    public Object invoke() {
        Object object;
        if (((Iterator)this_.iter).hasNext()) {
            this_.iter = null;
            datalog$partv$pv__18118$fn__18119 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)new datalog$partv$pv__18118$fn__18119$fn__18120(this_.iter, this_.n)).invoke(), ((IFn)this_.pv).invoke(this_.iter));
        } else {
            object = null;
        }
        return object;
    }
}

