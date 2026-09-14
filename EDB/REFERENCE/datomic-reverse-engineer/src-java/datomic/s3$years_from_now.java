/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.util.Date;

public final class s3$years_from_now
extends AFunction {
    public static Object invokeStatic(Object n) {
        Object object = n;
        n = null;
        return new Date(RT.longCast((Object)Numbers.add((Object)Numbers.multiply((Object)Numbers.multiply((Object)Numbers.multiply((Object)Numbers.multiply((Object)Numbers.multiply((Object)object, (long)1000L), (long)60L), (long)60L), (long)24L), (long)365L), (long)System.currentTimeMillis())));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3$years_from_now.invokeStatic(object2);
    }
}

