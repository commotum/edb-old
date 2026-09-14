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
import datomic.index.DirNode;

public final class index$dir_node
extends AFunction {
    public static Object invokeStatic(Object keydata, Object segids, Object offsets, Object counts) {
        Object object = keydata;
        keydata = null;
        Object object2 = segids;
        Object object3 = offsets;
        offsets = null;
        Object object4 = counts;
        counts = null;
        Object object5 = segids;
        segids = null;
        return new DirNode(object, object2, object3, object4, RT.object_array((Object)RT.alength((Object[])((Object[])object5))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return index$dir_node.invokeStatic(object5, object6, object7, object8);
    }
}

