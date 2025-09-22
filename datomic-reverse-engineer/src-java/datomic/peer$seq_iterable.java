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
import datomic.peer$seq_iterable$reify__21673;

public final class peer$seq_iterable
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 720, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object aseq) {
        Object object = aseq;
        aseq = null;
        return ((IObj)new peer$seq_iterable$reify__21673(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$seq_iterable.invokeStatic(object2);
    }
}

