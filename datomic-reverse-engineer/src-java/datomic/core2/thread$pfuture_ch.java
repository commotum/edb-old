/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.thread$pfuture_ch$fn__21016;

public final class thread$pfuture_ch
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.thread", (String)"pfuture-ch");
    public static final Var const__1 = RT.var((String)"datomic.core2.thread", (String)"result-chan");
    public static final Var const__2 = RT.var((String)"datomic.core2.thread", (String)"pfuture");

    public static Object invokeStatic(Object f, Object exec, Object ch) {
        Object object = f;
        f = null;
        Object object2 = exec;
        exec = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new thread$pfuture_ch$fn__21016(object, ch), object2);
        Object var2_2 = null;
        return ch;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return thread$pfuture_ch.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object f, Object exec) {
        Object object = f;
        f = null;
        Object object2 = exec;
        exec = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$pfuture_ch.invokeStatic(object3, object4);
    }
}

