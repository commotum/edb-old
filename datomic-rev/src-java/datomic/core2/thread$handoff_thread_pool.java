/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class thread$handoff_thread_pool
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"core-threads");
    public static final Object const__7 = 2L;
    public static final Keyword const__8 = RT.keyword(null, (String)"name");
    public static final Keyword const__9 = RT.keyword(null, (String)"max-threads");
    public static final Keyword const__10 = RT.keyword(null, (String)"metrics?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"name");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"max-threads");
    public static final Var const__17 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");
    public static final Var const__18 = RT.var((String)"datomic.core2.thread", (String)"observable-thread-pool");

    public static Object invokeStatic(Object p__21068) {
        Object object;
        Object object2;
        Object object3 = p__21068;
        p__21068 = null;
        Object map__21069 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21069);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21069);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21069;
                map__21069 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21069);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21069;
                    map__21069 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21069;
            map__21069 = null;
        }
        Object map__210692 = object2;
        Object core_threads = RT.get((Object)map__210692, (Object)const__6, (Object)const__7);
        Object name = RT.get((Object)map__210692, (Object)const__8);
        Object max_threads = RT.get((Object)map__210692, (Object)const__9);
        Object object9 = map__210692;
        map__210692 = null;
        Object metrics_QMARK_ = RT.get((Object)object9, (Object)const__10, (Object)Boolean.TRUE);
        Object object10 = name;
        if (object10 == null || object10 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__11.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke((Object)const__13))));
        }
        Object object11 = max_threads;
        if (object11 == null || object11 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__11.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke((Object)const__14))));
        }
        Object object12 = core_threads;
        core_threads = null;
        Object object13 = max_threads;
        max_threads = null;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(RT.intCast((Object)object12), RT.intCast((Object)object13), 60L, TimeUnit.SECONDS, (BlockingQueue<Runnable>)new SynchronousQueue(), (ThreadFactory)((IFn)const__17.getRawRoot()).invoke(name));
        Object object14 = metrics_QMARK_;
        metrics_QMARK_ = null;
        if (object14 != null && object14 != Boolean.FALSE) {
            ThreadPoolExecutor threadPoolExecutor = pool;
            pool = null;
            Object object15 = name;
            name = null;
            object = ((IFn)const__18.getRawRoot()).invoke((Object)threadPoolExecutor, object15);
        } else {
            object = pool;
            pool = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return thread$handoff_thread_pool.invokeStatic(object2);
    }
}

