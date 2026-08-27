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
import datomic.Connection;

public final class api$sync_excise
extends AFunction {
    public static Object invokeStatic(Object connection, Object t) {
        Object object = connection;
        connection = null;
        Object object2 = t;
        t = null;
        return ((Connection)object).syncExcise(RT.longCast((Object)((Number)object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return api$sync_excise.invokeStatic(object3, object4);
    }
}

