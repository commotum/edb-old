/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.iter.Iter;

public final class iter$reduce
extends AFunction {
    public static Object invokeStatic(Object f, Object init, Object iter2) {
        Object object = init;
        init = null;
        Object ret = object;
        Object object2 = iter2;
        iter2 = null;
        Object iter3 = object2;
        while (true) {
            Object object3 = iter3;
            if (object3 == null || object3 == Boolean.FALSE) break;
            Object object4 = ret;
            ret = null;
            Object object5 = ((IFn)f).invoke(object4, ((Iter)iter3).get());
            Object object6 = iter3;
            iter3 = null;
            iter3 = ((Iter)object6).next();
            ret = object5;
        }
        Object var3_3 = null;
        return ret;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return iter$reduce.invokeStatic(object4, object5, object6);
    }
}

