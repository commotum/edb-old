/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.index.RootNode;

public final class index$fn__15012$__GT_RootNode__15014
extends AFunction {
    public Object invoke(Object keydata, Object dirids, Object dirs) {
        Object object = keydata;
        keydata = null;
        Object object2 = dirids;
        dirids = null;
        Object object3 = dirs;
        dirs = null;
        return new RootNode(object, object2, object3);
    }
}

