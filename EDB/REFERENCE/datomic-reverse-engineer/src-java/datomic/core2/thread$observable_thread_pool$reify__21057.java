/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public final class thread$observable_thread_pool$reify__21057
implements RejectedExecutionHandler,
IObj {
    final IPersistentMap __meta;
    Object rejected_metric;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Object const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;

    public thread$observable_thread_pool$reify__21057(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.rejected_metric = object;
    }

    public thread$observable_thread_pool$reify__21057(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new thread$observable_thread_pool$reify__21057(iPersistentMap, this.rejected_metric);
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public void rejectedExecution(Runnable _, ThreadPoolExecutor _2) {
        Object object;
        Object object2 = const__1.getRawRoot();
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Impl) {
                object = ((Impl)object2).metric_STAR_((Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, this.rejected_metric, const__3, const__4, const__5, const__6}));
                throw (Throwable)new RejectedExecutionException();
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        object = const__0.getRawRoot().invoke(object2, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, this.rejected_metric, const__3, const__4, const__5, const__6}));
        throw (Throwable)new RejectedExecutionException();
    }

    static {
        const__0 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__1 = RT.var((String)"cognitect.caster", (String)"instance");
        const__2 = RT.keyword(null, (String)"name");
        const__3 = RT.keyword(null, (String)"value");
        const__4 = 1L;
        const__5 = RT.keyword(null, (String)"units");
        const__6 = RT.keyword(null, (String)"count");
    }
}

