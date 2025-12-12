/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cleanup;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.queue.BlockingConsumer;
import datomic.queue.Consumer;
import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;

public final class Manager
implements Consumer,
BlockingConsumer,
IType {
    public final Object phantoms;
    public final Object queue;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;

    public Manager(Object object, Object object2) {
        this.phantoms = object;
        this.queue = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"phantoms")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentHashMap")})), (Object)((IObj)Symbol.intern(null, (String)"queue")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ReferenceQueue")})));
    }

    /*
     * Unable to fully structure code
     */
    public Object poll_b(Object or_else, Object msec) {
        v0 = this.queue;
        if (Util.classOf((Object)v0) == Manager.__cached_class__2) ** GOTO lbl6
        if (!(v0 instanceof BlockingConsumer)) {
            v0 = v0;
            Manager.__cached_class__2 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = msec;
            msec = null;
            v2 = Manager.const__3.getRawRoot().invoke(v0, or_else, v1);
        } else {
            v3 = msec;
            msec = null;
            v2 = ref = ((BlockingConsumer)v0).poll_b(or_else, v3);
        }
        if (Util.equiv((Object)ref, (Object)or_else)) {
            v4 = or_else;
            or_else = null;
        } else {
            f = ((ConcurrentHashMap)this.phantoms).remove(ref);
            v5 = ref;
            ref = null;
            ((Reference)v5).clear();
            v4 = f;
            f = null;
        }
        return v4;
    }

    /*
     * Unable to fully structure code
     */
    public Object take() {
        v0 = this.queue;
        if (Util.classOf((Object)v0) == Manager.__cached_class__1) ** GOTO lbl6
        if (!(v0 instanceof BlockingConsumer)) {
            v0 = v0;
            Manager.__cached_class__1 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = Manager.const__2.getRawRoot().invoke(v0);
        } else {
            v1 = ((BlockingConsumer)v0).take();
        }
        ref = v1;
        f = ((ConcurrentHashMap)this.phantoms).remove(ref);
        v2 = ref;
        ref = null;
        ((Reference)v2).clear();
        var2_2 = null;
        return f;
    }

    /*
     * Unable to fully structure code
     */
    public Object poll_nb(Object or_else) {
        v0 = this.queue;
        if (Util.classOf((Object)v0) == Manager.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof Consumer)) {
            v0 = v0;
            Manager.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = Manager.const__0.getRawRoot().invoke(v0, or_else);
        } else {
            v1 = ref = ((Consumer)v0).poll_nb(or_else);
        }
        if (Util.equiv((Object)ref, (Object)or_else)) {
            v2 = or_else;
            or_else = null;
        } else {
            f = ((ConcurrentHashMap)this.phantoms).remove(ref);
            v3 = ref;
            ref = null;
            ((Reference)v3).clear();
            v2 = f;
            var3_3 = null;
        }
        return v2;
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"poll-nb");
        const__2 = RT.var((String)"datomic.queue", (String)"take");
        const__3 = RT.var((String)"datomic.queue", (String)"poll-b");
    }
}

