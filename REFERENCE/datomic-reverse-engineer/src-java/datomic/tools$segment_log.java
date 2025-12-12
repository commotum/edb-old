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
import datomic.tools$segment_log$fn__21844;

public final class tools$segment_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"connection-resources");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        Object cr = ((IFn)const__0.getRawRoot()).invoke(object);
        long n = 0L;
        while (true) {
            if (10L < n) {
                throw (Throwable)new RuntimeException("Retry limit exceeded segmenting log.");
            }
            Object object2 = ((IFn)new tools$segment_log$fn__21844(cr)).invoke();
            if (object2 != null && object2 != Boolean.FALSE) break;
            n = Numbers.inc((long)n);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$segment_log.invokeStatic(object2);
    }
}

