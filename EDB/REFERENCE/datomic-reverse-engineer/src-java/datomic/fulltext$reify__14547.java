/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentVector
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentVector;
import datomic.impl.clusterfs.IClusterFS;
import java.util.Collection;

public final class fulltext$reify__14547
implements IClusterFS,
IObj {
    final IPersistentMap __meta;

    public fulltext$reify__14547(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fulltext$reify__14547() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fulltext$reify__14547(iPersistentMap);
    }

    public Collection getFiles() {
        return (Collection)PersistentVector.EMPTY;
    }
}

