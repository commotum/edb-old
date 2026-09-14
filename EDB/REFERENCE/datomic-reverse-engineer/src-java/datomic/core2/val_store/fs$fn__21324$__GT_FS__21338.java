/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import datomic.core2.val_store.fs.FS;

public final class fs$fn__21324$__GT_FS__21338
extends AFunction {
    public Object invoke(Object get_pool, Object put_pool, Object path2, Object delete_pool) {
        Object object = get_pool;
        get_pool = null;
        Object object2 = put_pool;
        put_pool = null;
        Object object3 = path2;
        path2 = null;
        Object object4 = delete_pool;
        delete_pool = null;
        return new FS(object, object2, object3, object4);
    }
}

