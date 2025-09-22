/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOOL
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import java.util.Comparator;

public final class btset$comp
extends AFunction
implements IFn.OOOL {
    public static long invokeStatic(Object cmp, Object x, Object y) {
        int n;
        Object object = cmp;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = cmp;
            cmp = null;
            Object object3 = x;
            x = null;
            Object object4 = y;
            y = null;
            n = ((Comparator)object2).compare(object3, object4);
        } else {
            Object object5 = x;
            x = null;
            Object object6 = y;
            y = null;
            n = ((Comparable)object5).compareTo(object6);
        }
        return n;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return new Long(btset$comp.invokeStatic(object4, object5, object6));
    }

    public final long invokePrim(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return btset$comp.invokeStatic(object4, object5, object6);
    }
}

