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
import datomic.queue.BlockingConsumer;
import datomic.queue.Consumer;

public final class queue$poll
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object source, Object or_else, Object msec) {
        Object object;
        Object object2 = source;
        source = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__2) {
            if (object3 instanceof BlockingConsumer) {
                Object object4 = or_else;
                or_else = null;
                Object object5 = msec;
                msec = null;
                object = ((BlockingConsumer)object3).poll_b(object4, object5);
                return object;
            }
            object3 = object3;
            __cached_class__2 = Util.classOf((Object)object3);
        }
        Object object6 = or_else;
        or_else = null;
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
        return queue$poll.invokeStatic(object4, object5, object6);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object source, Object or_else) {
        Object object;
        Object object2 = source;
        source = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__1) {
            if (object3 instanceof Consumer) {
                Object object4 = or_else;
                or_else = null;
                object = ((Consumer)object3).poll_nb(object4);
                return object;
            }
            object3 = object3;
            __cached_class__1 = Util.classOf((Object)object3);
        }
        Object object5 = or_else;
        or_else = null;
        object = const__0.getRawRoot().invoke(object3, object5);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return queue$poll.invokeStatic(object3, object4);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object source) {
        Object object;
        Object object2 = source;
        source = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof Consumer) {
                object = ((Consumer)object3).poll_nb(null);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__0.getRawRoot().invoke(object3, null);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return queue$poll.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"poll-nb");
        const__1 = RT.var((String)"datomic.queue", (String)"poll-b");
    }
}

