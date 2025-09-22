/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;

public final class integrity$clusterfs_path_reachability$fn__22408$fn__22413
extends AFunction {
    Object filename;
    Object olookup;
    Object progress;

    public integrity$clusterfs_path_reachability$fn__22408$fn__22413(Object object, Object object2, Object object3) {
        this.filename = object;
        this.olookup = object2;
        this.progress = object3;
    }

    public Object invoke(Object ckey) {
        Object object = ckey;
        Object object2 = ckey;
        ckey = null;
        IPersistentVector item = Tuple.create((Object)this.filename, (Object)object, (Object)(RT.booleanCast((Object)RT.get((Object)this.olookup, (Object)object2)) ? Boolean.TRUE : Boolean.FALSE));
        Object object3 = this.progress;
        if (object3 != null && object3 != Boolean.FALSE) {
            ((IFn)this.progress).invoke((Object)item);
        }
        Object var2_2 = null;
        return item;
    }
}

