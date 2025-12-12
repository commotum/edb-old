/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.valcache_direct.ValcacheDirect;

public final class valcache_direct$fn__9906$__GT_ValcacheDirect__9924
extends AFunction {
    public Object invoke(Object root, Object shutdown_fn, Object puts_pool2) {
        Object object = root;
        root = null;
        Object object2 = shutdown_fn;
        shutdown_fn = null;
        Object object3 = puts_pool2;
        puts_pool2 = null;
        return new ValcacheDirect(object, object2, object3);
    }
}

