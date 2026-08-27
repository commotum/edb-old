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
import datomic.clusterfs.ClusterFS;

public final class clusterfs$fn__14206$__GT_ClusterFS__14225
extends AFunction {
    public Object invoke(Object dir, Object chunk_size) {
        Object object = dir;
        dir = null;
        Object object2 = chunk_size;
        chunk_size = null;
        return new ClusterFS(object, RT.intCast((Object)((Number)object2)));
    }
}

