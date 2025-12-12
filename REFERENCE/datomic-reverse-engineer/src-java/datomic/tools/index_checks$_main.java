/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.tools.index_checks$_main$fn__21957;

public final class index_checks$_main
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"shutdown");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        ((IFn)new index_checks$_main$fn__21957(object)).invoke();
        ((IFn)const__0.getRawRoot()).invoke((Object)Boolean.TRUE);
        System.exit(RT.intCast((long)0L));
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index_checks$_main.invokeStatic(object2);
    }
}

