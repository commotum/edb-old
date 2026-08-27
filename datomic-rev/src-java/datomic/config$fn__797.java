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

public final class config$fn__797
extends AFunction {
    public static Object invokeStatic(Object props) {
        Object object = props;
        props = null;
        return Numbers.num((long)RT.longCast((long)Math.round(Numbers.multiply((double)1.1, (Object)RT.get((Object)object, (Object)"datomic.ddbRequestTimeout")))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__797.invokeStatic(object2);
    }
}

