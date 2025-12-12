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
import datomic.config$fn__785$fn__786;

public final class config$fn__785
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"at-least");
    public static final Object const__1 = 0L;

    public static Object invokeStatic(Object n) {
        Object object;
        Object object2 = n;
        n = null;
        Object n2 = ((IFn)new config$fn__785$fn__786(object2)).invoke();
        Object object3 = ((IFn)((IFn)const__0.getRawRoot()).invoke(const__1)).invoke(n2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = n2;
            n2 = null;
        } else {
            object = Numbers.num((long)Numbers.add((long)2L, (long)Runtime.getRuntime().availableProcessors()));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__785.invokeStatic(object2);
    }
}

