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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class thread$thread_pool
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"name");
    public static final Keyword const__7 = RT.keyword(null, (String)"nthreads");
    public static final Keyword const__8 = RT.keyword(null, (String)"metrics?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"name");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"nthreads");
    public static final Var const__13 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");
    public static final Var const__14 = RT.var((String)"datomic.core2.thread", (String)"observable-thread-pool");

    public static Object invokeStatic(Object p__21062) {
        Object object;
        Object object2;
        Object object3 = p__21062;
        p__21062 = null;
        Object map__21063 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21063);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21063);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21063;
                map__21063 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21063);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21063;
                    map__21063 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21063;
            map__21063 = null;
        }
        Object map__210632 = object2;
        Object name = RT.get((Object)map__210632, (Object)const__6);
        Object nthreads = RT.get((Object)map__210632, (Object)const__7);
        Object object9 = map__210632;
        map__210632 = null;
        Object metrics_QMARK_ = RT.get((Object)object9, (Object)const__8, (Object)Boolean.TRUE);
        Object object10 = name;
        if (object10 == null || object10 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__11))));
        }
        Object object11 = nthreads;
        if (object11 == null || object11 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__12))));
        }
        Object object12 = nthreads;
        nthreads = null;
        ExecutorService pool = Executors.newFixedThreadPool(RT.intCast((Object)((Number)object12)), (ThreadFactory)((IFn)const__13.getRawRoot()).invoke(name));
        Object object13 = metrics_QMARK_;
        metrics_QMARK_ = null;
        if (object13 != null && object13 != Boolean.FALSE) {
            ExecutorService executorService = pool;
            pool = null;
            Object object14 = name;
            name = null;
            object = ((IFn)const__14.getRawRoot()).invoke((Object)executorService, object14);
        } else {
            object = pool;
            pool = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return thread$thread_pool.invokeStatic(object2);
    }
}

