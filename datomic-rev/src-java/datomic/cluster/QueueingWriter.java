/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cluster;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.AsyncWriter;
import datomic.cluster.ClusteredStore;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class QueueingWriter
implements AsyncWriter,
ClusteredStore,
IType {
    public final Object cluster;
    public final Object done_reason;
    public final Object bounding_timeout_msec;
    public final Object queue;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final AFn const__11;

    public QueueingWriter(Object object, Object object2, Object object3, Object object4) {
        this.cluster = object;
        this.done_reason = object2;
        this.bounding_timeout_msec = object3;
        this.queue = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"done-reason"), (Object)Symbol.intern(null, (String)"bounding-timeout-msec"), (Object)((IObj)Symbol.intern(null, (String)"queue")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.BlockingQueue")})));
    }

    public Object finish_writer() {
        if (!((BlockingQueue)this.queue).offer(const__11, RT.longCast((Object)((Number)this.bounding_timeout_msec)), TimeUnit.MILLISECONDS)) {
            throw (Throwable)new TimeoutException("Timed out waiting for queue");
        }
        return this.done_reason;
    }

    public Object sync_writes() {
        Object prom = ((IFn)const__8.getRawRoot()).invoke();
        if (!((BlockingQueue)this.queue).offer(RT.mapUniqueKeys((Object[])new Object[]{const__1, const__9, const__3, prom}), RT.longCast((Object)((Number)this.bounding_timeout_msec)), TimeUnit.MILLISECONDS)) {
            throw (Throwable)new TimeoutException("Timed out waiting for queue");
        }
        Object var1_1 = null;
        return prom;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object create_val(Object k, Object v) {
        Object object;
        QueueingWriter queueingWriter = this_;
        if (Util.classOf((Object)queueingWriter) != __cached_class__1) {
            if (queueingWriter instanceof ClusteredStore) {
                Object object2 = k;
                k = null;
                Object object3 = v;
                v = null;
                object = ((ClusteredStore)queueingWriter).create_val(const__7, object2, object3);
                return object;
            }
            queueingWriter = queueingWriter;
            __cached_class__1 = Util.classOf((Object)queueingWriter);
        }
        Object object4 = k;
        k = null;
        Object object5 = v;
        v = null;
        QueueingWriter this_ = null;
        object = const__4.getRawRoot().invoke((Object)queueingWriter, const__7, object4, object5);
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object create_val(Object priority, Object k, Object v) {
        block5: {
            block4: {
                v0 = ((IFn)QueueingWriter.const__0.getRawRoot()).invoke(this.done_reason);
                if (v0 == null || v0 == Boolean.FALSE) break block4;
                v1 = this.done_reason;
                break block5;
            }
            v2 = (BlockingQueue)this.queue;
            v3 = new Object[4];
            v3[0] = QueueingWriter.const__1;
            v3[1] = QueueingWriter.const__2;
            v3[2] = QueueingWriter.const__3;
            v4 = this.cluster;
            if (Util.classOf((Object)v4) == QueueingWriter.__cached_class__0) ** GOTO lbl16
            if (!(v4 instanceof ClusteredStore)) {
                v4 = v4;
                QueueingWriter.__cached_class__0 = Util.classOf((Object)v4);
lbl16:
                // 2 sources

                v5 = priority;
                priority = null;
                v6 = k;
                k = null;
                v7 = v;
                v = null;
                v8 = QueueingWriter.const__4.getRawRoot().invoke(v4, v5, v6, v7);
            } else {
                v9 = priority;
                priority = null;
                v10 = k;
                k = null;
                v11 = v;
                v = null;
                v8 = v3[3] = ((ClusteredStore)v4).create_val(v9, v10, v11);
            }
            if (v2.offer(RT.mapUniqueKeys((Object[])v3), RT.longCast((Object)((Number)this.bounding_timeout_msec)), TimeUnit.MILLISECONDS)) {
                this = null;
                v1 = ((IFn)QueueingWriter.const__5.getRawRoot()).invoke((Object)QueueingWriter.const__6);
            } else {
                throw (Throwable)new TimeoutException("Timed out waiting for queue");
            }
        }
        return v1;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"realized?");
        const__1 = RT.keyword(null, (String)"type");
        const__2 = RT.keyword(null, (String)"create-val");
        const__3 = RT.keyword(null, (String)"obj");
        const__4 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__5 = RT.var((String)"datomic.promise", (String)"delivered");
        const__6 = RT.keyword(null, (String)"created");
        const__7 = 3L;
        const__8 = RT.var((String)"clojure.core", (String)"promise");
        const__9 = RT.keyword(null, (String)"sync-writes");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"type"), RT.keyword(null, (String)"finish")});
    }
}

