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
import datomic.backup$uncached_storage_lookup$reify__20046;

public final class backup$uncached_storage_lookup
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 173, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object storage) {
        Object object = storage;
        storage = null;
        return ((IObj)new backup$uncached_storage_lookup$reify__20046(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$uncached_storage_lookup.invokeStatic(object2);
    }
}

