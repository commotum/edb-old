/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_hotrod.KVHotRod;

public final class kv_hotrod$fn__17801$__GT_KVHotRod__17810
extends AFunction {
    public Object invoke(Object cache2) {
        Object object = cache2;
        cache2 = null;
        return new KVHotRod(object);
    }
}

