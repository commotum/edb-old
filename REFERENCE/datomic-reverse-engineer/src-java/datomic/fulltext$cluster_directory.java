/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookup
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookup;
import datomic.impl.clusterfs.IClusterFS;
import datomic.impl.lucene.ClusterDirectory;

public final class fulltext$cluster_directory
extends AFunction {
    public static Object invokeStatic(Object clusterfs2, Object olookup) {
        Object object = clusterfs2;
        clusterfs2 = null;
        Object object2 = olookup;
        olookup = null;
        return new ClusterDirectory((IClusterFS)object, (ILookup)object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$cluster_directory.invokeStatic(object3, object4);
    }
}

