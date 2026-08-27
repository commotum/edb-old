/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.cluster$uncached_val_lookup$reify__10654;

public final class cluster$uncached_val_lookup
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 196, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object cs) {
        Object object = cs;
        cs = null;
        return ((IObj)new cluster$uncached_val_lookup$reify__10654(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$uncached_val_lookup.invokeStatic(object2);
    }
}

