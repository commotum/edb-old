/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.index.DirNode;

public final class index$fn__15018$__GT_DirNode__15020
extends AFunction {
    public Object invoke(Object keydata, Object segids, Object offsets, Object counts, Object segs) {
        Object object = keydata;
        keydata = null;
        Object object2 = segids;
        segids = null;
        Object object3 = offsets;
        offsets = null;
        Object object4 = counts;
        counts = null;
        Object object5 = segs;
        segs = null;
        return new DirNode(object, object2, object3, object4, object5);
    }
}

