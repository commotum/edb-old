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

public final class aws$client_config
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Object const__1 = RT.classForName((String)"com.amazonaws.ClientConfiguration");

    public static Object invokeStatic(Object args) {
        Object object = args;
        args = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws$client_config.invokeStatic(object2);
    }
}

