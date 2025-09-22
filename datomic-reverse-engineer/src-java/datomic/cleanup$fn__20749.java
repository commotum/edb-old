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
import datomic.cleanup$fn__20749$fn__20751;

public final class cleanup$fn__20749
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cleanup", (String)"create-manager");

    public static Object invokeStatic() {
        Object manager = ((IFn)const__0.getRawRoot()).invoke();
        Thread G__20750 = new Thread((Runnable)((Object)new cleanup$fn__20749$fn__20751(manager)));
        G__20750.setDaemon(Boolean.TRUE);
        G__20750.start();
        Object var0 = null;
        return manager;
    }

    public Object invoke() {
        return cleanup$fn__20749.invokeStatic();
    }
}

