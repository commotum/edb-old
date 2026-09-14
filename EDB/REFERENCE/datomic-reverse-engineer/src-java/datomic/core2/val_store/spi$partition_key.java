/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class spi$partition_key
extends AFunction {
    public static Object invokeStatic(Object s) {
        String string = (String)s;
        Object object = s;
        s = null;
        return string.substring(RT.intCast((long)Numbers.minus((long)((String)object).length(), (long)3L))).toLowerCase();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return spi$partition_key.invokeStatic(object2);
    }
}

