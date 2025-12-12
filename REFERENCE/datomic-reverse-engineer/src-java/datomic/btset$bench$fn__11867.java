/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.util.Random;
import java.util.TreeSet;

public final class btset$bench$fn__11867
extends AFunction {
    Object r;
    Object ts;
    Object x;

    public btset$bench$fn__11867(Object object, Object object2, Object object3) {
        this.r = object;
        this.ts = object2;
        this.x = object3;
    }

    public Object invoke() {
        long i = 0L;
        while (Numbers.lt((long)i, (Object)this.x)) {
            long n = ((Random)this.r).nextLong();
            Boolean bl = ((TreeSet)this.ts).add(Numbers.num((long)n)) ? Boolean.TRUE : Boolean.FALSE;
            ++i;
        }
        return this.ts;
    }
}

