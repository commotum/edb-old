/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.thread$observable_thread_pool$fn__21059;
import datomic.core2.thread$observable_thread_pool$reify__21057;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public final class thread$observable_thread_pool
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.thread", (String)"->ObservableThreadPool");
    public static final Var const__1 = RT.var((String)"datomic.core2.thread", (String)"clojure-name->metric-name");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final AFn const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 178, RT.keyword(null, (String)"column"), 7});

    public static Object invokeStatic(Object pool, Object name) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = pool;
        Object object2 = name;
        name = null;
        Object mname = ((IFn)const__1.getRawRoot()).invoke(object2);
        Object active_metric = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Pool.", mname, (Object)".Active"));
        Object queued_metric = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Pool.", mname, (Object)".Queued"));
        Object object3 = mname;
        mname = null;
        Object rejected_metric = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Pool.", object3, (Object)".Rejected"));
        Object object4 = pool;
        pool = null;
        Object object5 = rejected_metric;
        rejected_metric = null;
        ((ThreadPoolExecutor)object4).setRejectedExecutionHandler((RejectedExecutionHandler)((IObj)new thread$observable_thread_pool$reify__21057(null, object5)).withMeta((IPersistentMap)const__8));
        Object object6 = active_metric;
        active_metric = null;
        Object object7 = queued_metric;
        queued_metric = null;
        return iFn.invoke(object, (Object)new thread$observable_thread_pool$fn__21059(object6, object7));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$observable_thread_pool.invokeStatic(object3, object4);
    }
}

