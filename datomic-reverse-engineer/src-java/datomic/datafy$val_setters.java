/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datafy$val_setters$fn__17353;
import java.beans.Introspector;

public final class datafy$val_setters
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object bean_class) {
        Object object = bean_class;
        bean_class = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new datafy$val_setters$fn__17353(), (Object)PersistentArrayMap.EMPTY, (Object)Introspector.getBeanInfo((Class)object).getPropertyDescriptors());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$val_setters.invokeStatic(object2);
    }
}

