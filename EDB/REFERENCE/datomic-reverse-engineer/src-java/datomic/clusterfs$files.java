/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.impl.clusterfs.IClusterFS;

public final class clusterfs$files
extends AFunction {
    public static Object invokeStatic(Object clusterfs2) {
        Object object = clusterfs2;
        clusterfs2 = null;
        return ((IClusterFS)object).getFiles();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return clusterfs$files.invokeStatic(object2);
    }
}

