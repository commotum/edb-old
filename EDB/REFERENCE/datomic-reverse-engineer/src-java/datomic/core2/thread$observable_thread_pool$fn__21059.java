/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;
import java.util.concurrent.ThreadPoolExecutor;

public final class thread$observable_thread_pool$fn__21059
extends AFunction {
    Object active_metric;
    Object queued_metric;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;

    public thread$observable_thread_pool$fn__21059(Object object, Object object2) {
        this.active_metric = object;
        this.queued_metric = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object pool) {
        v0 = thread$observable_thread_pool$fn__21059.const__1.getRawRoot();
        if (Util.classOf((Object)v0) == thread$observable_thread_pool$fn__21059.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Impl)) {
            v0 = v0;
            thread$observable_thread_pool$fn__21059.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = thread$observable_thread_pool$fn__21059.const__0.getRawRoot().invoke(v0, (Object)RT.mapUniqueKeys((Object[])new Object[]{thread$observable_thread_pool$fn__21059.const__2, this.active_metric, thread$observable_thread_pool$fn__21059.const__3, ((ThreadPoolExecutor)pool).getActiveCount(), thread$observable_thread_pool$fn__21059.const__4, thread$observable_thread_pool$fn__21059.const__5}));
        } else {
            v1 = ((Impl)v0).metric_STAR_((Object)RT.mapUniqueKeys((Object[])new Object[]{thread$observable_thread_pool$fn__21059.const__2, this.active_metric, thread$observable_thread_pool$fn__21059.const__3, ((ThreadPoolExecutor)pool).getActiveCount(), thread$observable_thread_pool$fn__21059.const__4, thread$observable_thread_pool$fn__21059.const__5}));
        }
        v2 = pool;
        pool = null;
        this = null;
        return ((IFn)thread$observable_thread_pool$fn__21059.const__6.getRawRoot()).invoke(v2, this.queued_metric);
    }

    static {
        const__0 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__1 = RT.var((String)"cognitect.caster", (String)"instance");
        const__2 = RT.keyword(null, (String)"name");
        const__3 = RT.keyword(null, (String)"value");
        const__4 = RT.keyword(null, (String)"units");
        const__5 = RT.keyword(null, (String)"count");
        const__6 = RT.var((String)"datomic.core2.thread", (String)"cast-queue-metric");
    }
}

