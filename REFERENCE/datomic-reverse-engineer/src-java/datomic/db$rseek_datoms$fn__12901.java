/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.Tuple;
import datomic.impl.db.IDatum;

public final class db$rseek_datoms$fn__12901
extends AFunction {
    public Object invoke(Object d) {
        Number number = Numbers.num((long)((IDatum)d).getE());
        Integer n = ((IDatum)d).getA();
        Object object = d;
        d = null;
        return Tuple.create((Object)number, (Object)n, (Object)((IDatum)object).getV());
    }
}

