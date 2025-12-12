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

public final class iter$drop_while
extends AFunction {
    public static Object invokeStatic(Object p, Object iter2) {
        Object object;
        block2: {
            block1: {
                Object object2 = iter2;
                iter2 = null;
                Object iter3 = object2;
                while (true) {
                    Object object3 = iter3;
                    if (object3 == null || object3 == Boolean.FALSE) break block1;
                    Object object4 = ((IFn)p).invoke(((Iter)iter3).get());
                    if (object4 == null || object4 == Boolean.FALSE) break;
                    Object object5 = iter3;
                    iter3 = null;
                    iter3 = ((Iter)object5).next();
                }
                object = iter3;
                iter3 = null;
                break block2;
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return iter$drop_while.invokeStatic(object3, object4);
    }
}

