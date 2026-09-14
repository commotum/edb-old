/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import javax.crypto.Mac;

public final class crypto$fn__23364
extends AFunction {
    public static Object invokeStatic(Object algorithm) {
        Object object = algorithm;
        algorithm = null;
        return Mac.getInstance((String)object).getMacLength();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$fn__23364.invokeStatic(object2);
    }
}

