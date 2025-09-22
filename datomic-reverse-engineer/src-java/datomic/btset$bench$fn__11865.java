/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Random;

public final class btset$bench$fn__11865
extends AFunction {
    Object r;
    Object x;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"sorted-set");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");

    public btset$bench$fn__11865(Object object, Object object2) {
        this.r = object;
        this.x = object2;
    }

    public Object invoke() {
        Object bs = ((IFn)const__0.getRawRoot()).invoke();
        long i = 0L;
        while (Numbers.lt((long)i, (Object)this.x)) {
            long n = ((Random)this.r).nextLong();
            Object object = bs;
            bs = null;
            ++i;
            bs = ((IFn)const__3.getRawRoot()).invoke(object, (Object)Numbers.num((long)n));
        }
        Object var1_1 = null;
        return bs;
    }
}

