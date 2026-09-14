/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;

public final class common$maybe_class
extends AFunction {
    public static Object invokeStatic(Object cls) {
        Class clazz;
        try {
            Object object = cls;
            cls = null;
            clazz = RT.classForNameNonLoading((String)((String)object));
        }
        catch (ClassNotFoundException _) {
            clazz = null;
        }
        return clazz;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$maybe_class.invokeStatic(object2);
    }
}

