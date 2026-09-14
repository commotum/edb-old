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
import datomic.index.RootNode;

public final class index$root_node
extends AFunction {
    public static Object invokeStatic(Object keydata, Object dirids) {
        Object object = keydata;
        keydata = null;
        Object object2 = dirids;
        Object object3 = dirids;
        dirids = null;
        return new RootNode(object, object2, RT.object_array((Object)RT.alength((Object[])((Object[])object3))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$root_node.invokeStatic(object3, object4);
    }
}

