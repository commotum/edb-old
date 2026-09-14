/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class spi$result
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"promise-chan");
    public static final Var const__1 = RT.var((String)"clojure.core.async", (String)"put!");
    public static final Var const__2 = RT.var((String)"clojure.core.async", (String)"close!");

    public static Object invokeStatic(Object v) {
        Object ch = ((IFn)const__0.getRawRoot()).invoke();
        Object object = v;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = v;
            v = null;
            ((IFn)const__1.getRawRoot()).invoke(ch, object2);
        } else {
            ((IFn)const__2.getRawRoot()).invoke(ch);
        }
        Object var1_1 = null;
        return ch;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return spi$result.invokeStatic(object2);
    }
}

