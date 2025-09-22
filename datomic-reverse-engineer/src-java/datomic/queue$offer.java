/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.queue.BlockingProducer;
import datomic.queue.Producer;

public final class queue$offer
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object sink, Object item, Object msec) {
        Object object;
        Object object2 = sink;
        sink = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__1) {
            if (object3 instanceof BlockingProducer) {
                Object object4 = item;
                item = null;
                Object object5 = msec;
                msec = null;
                object = ((BlockingProducer)object3).offer_b(object4, object5);
                return object;
            }
            object3 = object3;
            __cached_class__1 = Util.classOf((Object)object3);
        }
        Object object6 = item;
        item = null;
        Object object7 = msec;
        msec = null;
        object = const__1.getRawRoot().invoke(object3, object6, object7);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return queue$offer.invokeStatic(object4, object5, object6);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object sink, Object item) {
        Object object;
        Object object2 = sink;
        sink = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof Producer) {
                Object object4 = item;
                item = null;
                object = ((Producer)object3).offer_nb(object4);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = item;
        item = null;
        object = const__0.getRawRoot().invoke(object3, object5);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return queue$offer.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"offer-nb");
        const__1 = RT.var((String)"datomic.queue", (String)"offer-b");
    }
}

