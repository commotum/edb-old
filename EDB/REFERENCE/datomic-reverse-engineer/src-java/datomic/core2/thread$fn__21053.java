/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;
import java.util.concurrent.ThreadPoolExecutor;

public final class thread$fn__21053
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Keyword const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object pool, Object metric) {
        Object object;
        Object object2 = const__1.getRawRoot();
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Impl) {
                Object[] objectArray = new Object[6];
                objectArray[0] = const__2;
                Object object3 = metric;
                metric = null;
                objectArray[1] = object3;
                objectArray[2] = const__3;
                Object object4 = pool;
                pool = null;
                objectArray[3] = RT.count(((ThreadPoolExecutor)object4).getQueue());
                objectArray[4] = const__5;
                objectArray[5] = const__6;
                object = ((Impl)object2).metric_STAR_((Object)RT.mapUniqueKeys((Object[])objectArray));
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object[] objectArray = new Object[6];
        objectArray[0] = const__2;
        Object object5 = metric;
        metric = null;
        objectArray[1] = object5;
        objectArray[2] = const__3;
        Object object6 = pool;
        pool = null;
        objectArray[3] = RT.count(((ThreadPoolExecutor)object6).getQueue());
        objectArray[4] = const__5;
        objectArray[5] = const__6;
        object = const__0.getRawRoot().invoke(object2, (Object)RT.mapUniqueKeys((Object[])objectArray));
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$fn__21053.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__1 = RT.var((String)"cognitect.caster", (String)"instance");
        const__2 = RT.keyword(null, (String)"name");
        const__3 = RT.keyword(null, (String)"value");
        const__5 = RT.keyword(null, (String)"units");
        const__6 = RT.keyword(null, (String)"count");
    }
}

