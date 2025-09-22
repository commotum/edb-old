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

public final class config$fn__799
extends AFunction {
    public static Object invokeStatic(Object props) {
        Object object = props;
        props = null;
        return RT.get((Object)object, (Object)"datomic.ddbSocketTimeout");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__799.invokeStatic(object2);
    }
}

