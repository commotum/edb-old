/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.Counted
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.queue;

import clojure.lang.Counted;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.queue.BlockingProducer;
import java.io.Closeable;
import java.io.IOException;

public final class DelayingQueue
implements BlockingProducer,
Closeable,
Counted,
IType {
    public final Object delay;
    public final Object delay_queue;
    public final Object thread;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;

    public DelayingQueue(Object object, Object object2, Object object3) {
        this.delay = object;
        this.delay_queue = object2;
        this.thread = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"delay"), (Object)Symbol.intern(null, (String)"delay-queue"), (Object)((IObj)Symbol.intern(null, (String)"thread")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Thread")})));
    }

    /*
     * Unable to fully structure code
     */
    public void close() throws IOException {
        v0 = this.delay_queue;
        if (Util.classOf((Object)v0) == DelayingQueue.__cached_class__1) ** GOTO lbl6
        if (!(v0 instanceof BlockingProducer)) {
            v0 = v0;
            DelayingQueue.__cached_class__1 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = DelayingQueue.const__0.getRawRoot().invoke(v0, this.delay_queue);
        } else {
            v1 = ((BlockingProducer)v0).put(this.delay_queue);
        }
        ((Thread)this.thread).join();
    }

    public int count() {
        return RT.count((Object)this.delay_queue);
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object put(Object item) {
        Object object;
        Object object2 = this_.delay_queue;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof BlockingProducer) {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__1;
                Object object3 = item;
                item = null;
                objectArray[1] = object3;
                objectArray[2] = const__2;
                objectArray[3] = Numbers.add((Object)this_.delay, (long)System.currentTimeMillis());
                object = ((BlockingProducer)object2).put(RT.mapUniqueKeys((Object[])objectArray));
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        Object object4 = item;
        item = null;
        objectArray[1] = object4;
        objectArray[2] = const__2;
        objectArray[3] = Numbers.add((Object)this_.delay, (long)System.currentTimeMillis());
        DelayingQueue this_ = null;
        object = const__0.getRawRoot().invoke(object2, (Object)RT.mapUniqueKeys((Object[])objectArray));
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"put");
        const__1 = RT.keyword(null, (String)"item");
        const__2 = RT.keyword(null, (String)"timestamp");
    }
}

