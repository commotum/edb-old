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
import java.util.List;

public final class qtune$cvars
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object c) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = const__1.getRawRoot();
        if (((IFn)const__4.getRawRoot()).invoke(c) instanceof List) {
            Object object3 = c;
            c = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object3);
        } else {
            object = c;
            c = null;
        }
        return iFn.invoke(object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return qtune$cvars.invokeStatic(object2);
    }
}

