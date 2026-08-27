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
import datomic.index$conjable_on_channel$reify__15423;

public final class index$conjable_on_channel
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 1343, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object ch) {
        Object object = ch;
        ch = null;
        return ((IObj)new index$conjable_on_channel$reify__15423(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$conjable_on_channel.invokeStatic(object2);
    }
}

