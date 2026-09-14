/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cleanup$run_queue_loop$fn__20746;
import datomic.queue.BlockingConsumer;

public final class cleanup$run_queue_loop
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object q, Object error_handler) {
        while (true) {
            if (Util.classOf((Object)(v0 = q)) == cleanup$run_queue_loop.__cached_class__0) ** GOTO lbl6
            if (!(v0 instanceof BlockingConsumer)) {
                v0 = v0;
                cleanup$run_queue_loop.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
                // 2 sources

                v1 = cleanup$run_queue_loop.const__0.getRawRoot().invoke(v0);
            } else {
                v1 = ((BlockingConsumer)v0).take();
            }
            v2 = f = v1;
            f = null;
            ((IFn)new cleanup$run_queue_loop$fn__20746(error_handler, v2)).invoke();
        }
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cleanup$run_queue_loop.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"take");
    }
}

