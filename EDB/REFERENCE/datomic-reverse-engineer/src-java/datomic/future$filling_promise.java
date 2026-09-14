/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.future$filling_promise$f__10145;

public final class future$filling_promise
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"promise-chan");

    public static Object invokeStatic(Object f) {
        future$filling_promise$f__10145 f2;
        Object ch = ((IFn)const__0.getRawRoot()).invoke();
        Object object = f;
        f = null;
        future$filling_promise$f__10145 future$filling_promise$f__10145 = f2 = new future$filling_promise$f__10145(object, ch);
        f2 = null;
        Object object2 = ch;
        ch = null;
        return Tuple.create((Object)((Object)future$filling_promise$f__10145), (Object)object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return future$filling_promise.invokeStatic(object2);
    }
}

