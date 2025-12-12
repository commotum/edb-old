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
import datomic.external_sort$temp_file_io$reify__14398;

public final class external_sort$temp_file_io
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 24, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object dir) {
        Object object = dir;
        dir = null;
        return ((IObj)new external_sort$temp_file_io$reify__14398(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return external_sort$temp_file_io.invokeStatic(object2);
    }
}

