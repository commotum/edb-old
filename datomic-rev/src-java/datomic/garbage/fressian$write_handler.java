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
package datomic.garbage;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.garbage.fressian$write_handler$reify__16633;

public final class fressian$write_handler
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 19, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object tag) {
        Object object = tag;
        tag = null;
        return ((IObj)new fressian$write_handler$reify__16633(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$write_handler.invokeStatic(object2);
    }
}

