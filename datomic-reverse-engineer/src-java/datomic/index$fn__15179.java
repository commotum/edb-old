/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Namespace
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Namespace;
import clojure.lang.RT;

public final class index$fn__15179
extends AFunction {
    public static final Object const__0 = RT.classForName((String)"datomic.index.IBinarySearch");

    public static Object invokeStatic() {
        return ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.index.IBinarySearch"));
    }

    public Object invoke() {
        return index$fn__15179.invokeStatic();
    }
}

