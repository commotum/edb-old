/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cluster$queueing_writer$fn__10722;
import datomic.cluster.QueueingWriter;
import java.util.concurrent.ArrayBlockingQueue;

public final class cluster$queueing_writer
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.promise", (String)"settable-future");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"future-call");

    public static Object invokeStatic(Object cluster2, Object par, Object bounding_timeout_msec, Object progress) {
        Object object = par;
        par = null;
        ArrayBlockingQueue queue2 = new ArrayBlockingQueue(RT.intCast((Object)((Number)object)));
        Object done_reason = ((IFn)const__0.getRawRoot()).invoke();
        Object object2 = cluster2;
        cluster2 = null;
        Object object3 = bounding_timeout_msec;
        bounding_timeout_msec = null;
        QueueingWriter writer2 = new QueueingWriter(object2, done_reason, object3, queue2);
        Object object4 = done_reason;
        done_reason = null;
        Object object5 = progress;
        progress = null;
        ArrayBlockingQueue arrayBlockingQueue = queue2;
        queue2 = null;
        ((IFn)const__1.getRawRoot()).invoke((Object)new cluster$queueing_writer$fn__10722(object4, object5, arrayBlockingQueue));
        QueueingWriter queueingWriter = writer2;
        writer2 = null;
        return queueingWriter;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return cluster$queueing_writer.invokeStatic(object5, object6, object7, object8);
    }
}

