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

public final class thread$cached_thread_pool
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"name");
    public static final Keyword const__7 = RT.keyword(null, (String)"metrics?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"name");
    public static final Var const__11 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");
    public static final Var const__12 = RT.var((String)"datomic.core2.thread", (String)"observable-thread-pool");

    public static Object invokeStatic(Object p__21065) {
        Object object;
        Object object2;
        Object object3 = p__21065;
        p__21065 = null;
        Object map__21066 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21066);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21066);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21066;
                map__21066 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21066);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21066;
                    map__21066 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21066;
            map__21066 = null;
        }
        Object map__210662 = object2;
        Object name = RT.get((Object)map__210662, (Object)const__6);
        Object object9 = map__210662;
        map__210662 = null;
        Object metrics_QMARK_ = RT.get((Object)object9, (Object)const__7, (Object)Boolean.TRUE);
        Object object10 = name;
        if (object10 == null || object10 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__8.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__9.getRawRoot()).invoke((Object)const__10))));
        }
        ExecutorService pool = Executors.newCachedThreadPool((ThreadFactory)((IFn)const__11.getRawRoot()).invoke(name));
        Object object11 = metrics_QMARK_;
        metrics_QMARK_ = null;
        if (object11 != null && object11 != Boolean.FALSE) {
            ExecutorService executorService = pool;
            pool = null;
            Object object12 = name;
            name = null;
            object = ((IFn)const__12.getRawRoot()).invoke((Object)executorService, object12);
        } else {
            object = pool;
            pool = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return thread$cached_thread_pool.invokeStatic(object2);
    }
}

