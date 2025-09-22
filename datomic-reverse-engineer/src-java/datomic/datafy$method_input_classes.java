/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$method_input_classes
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((Method)object).getParameterTypes());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$method_input_classes.invokeStatic(object2);
    }
}

