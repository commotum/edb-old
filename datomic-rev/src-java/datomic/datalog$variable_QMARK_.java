/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.config.Symbolish;

public final class datalog$variable_QMARK_
extends AFunction {
    private static Class __cached_class__0;
    public static final Object const__1;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object x) {
        v0 = (IFn)datalog$variable_QMARK_.const__2.getRawRoot();
        v1 = x;
        x = null;
        v2 = v1;
        if (Util.classOf((Object)v1) == datalog$variable_QMARK_.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof Symbolish)) {
            v2 = v2;
            datalog$variable_QMARK_.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = datalog$variable_QMARK_.const__3.getRawRoot().invoke(v2);
        } else {
            v3 = ((Symbolish)v2).sym_name();
        }
        return Util.equiv((Object)datalog$variable_QMARK_.const__1, (Object)v0.invoke(v3)) != false ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$variable_QMARK_.invokeStatic(object2);
    }

    static {
        const__1 = Character.valueOf('?');
        const__2 = RT.var((String)"clojure.core", (String)"first");
        const__3 = RT.var((String)"datomic.config", (String)"sym-name");
    }
}

