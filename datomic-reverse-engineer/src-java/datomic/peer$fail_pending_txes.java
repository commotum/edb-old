/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.queue.Clear;

public final class peer$fail_pending_txes
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__14;
    public static final Var const__15;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object unsent_updates_queue, Object pending_txes) {
        v0 = unsent_updates_queue;
        unsent_updates_queue = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == peer$fail_pending_txes.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof Clear)) {
            v1 = v1;
            peer$fail_pending_txes.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = peer$fail_pending_txes.const__0.getRawRoot().invoke(v1);
        } else {
            v2 = ((Clear)v1).clear();
        }
        seq_21489 = ((IFn)peer$fail_pending_txes.const__1.getRawRoot()).invoke(((IFn)peer$fail_pending_txes.const__2.getRawRoot()).invoke(pending_txes));
        chunk_21490 = null;
        count_21491 = 0L;
        i_21492 = 0L;
        while (true) {
            if (i_21492 < count_21491) {
                v3 = k = ((Indexed)chunk_21490).nth(RT.intCast((long)i_21492));
                k = null;
                v4 = temp__5457__auto__21494 = ((IFn)peer$fail_pending_txes.const__5.getRawRoot()).invoke(pending_txes, v3);
                if (v4 != null && v4 != Boolean.FALSE) {
                    v5 = temp__5457__auto__21494;
                    temp__5457__auto__21494 = null;
                    v6 = prom = v5;
                    prom = null;
                    ((IFn)peer$fail_pending_txes.const__6.getRawRoot()).invoke(v6, ((IFn)peer$fail_pending_txes.const__7.getRawRoot()).invoke());
                }
                v7 = seq_21489;
                seq_21489 = null;
                v8 = chunk_21490;
                chunk_21490 = null;
                ++i_21492;
                chunk_21490 = v8;
                seq_21489 = v7;
                continue;
            }
            v9 = seq_21489;
            seq_21489 = null;
            v10 = temp__5457__auto__21497 = ((IFn)peer$fail_pending_txes.const__1.getRawRoot()).invoke(v9);
            if (v10 == null || v10 == Boolean.FALSE) break;
            v11 = temp__5457__auto__21497;
            temp__5457__auto__21497 = null;
            seq_21489 = v11;
            v12 = ((IFn)peer$fail_pending_txes.const__9.getRawRoot()).invoke(seq_21489);
            if (v12 != null && v12 != Boolean.FALSE) {
                c__5719__auto__21495 = ((IFn)peer$fail_pending_txes.const__10.getRawRoot()).invoke(seq_21489);
                v13 = seq_21489;
                seq_21489 = null;
                v14 = c__5719__auto__21495;
                v15 = c__5719__auto__21495;
                c__5719__auto__21495 = null;
                i_21492 = RT.intCast((long)0L);
                count_21491 = RT.intCast((int)RT.count((Object)v15));
                chunk_21490 = v14;
                seq_21489 = ((IFn)peer$fail_pending_txes.const__11.getRawRoot()).invoke(v13);
                continue;
            }
            v16 = k = ((IFn)peer$fail_pending_txes.const__14.getRawRoot()).invoke(seq_21489);
            k = null;
            v17 = temp__5457__auto__21496 = ((IFn)peer$fail_pending_txes.const__5.getRawRoot()).invoke(pending_txes, v16);
            if (v17 != null && v17 != Boolean.FALSE) {
                v18 = temp__5457__auto__21496;
                temp__5457__auto__21496 = null;
                v19 = prom = v18;
                prom = null;
                ((IFn)peer$fail_pending_txes.const__6.getRawRoot()).invoke(v19, ((IFn)peer$fail_pending_txes.const__7.getRawRoot()).invoke());
            }
            v20 = seq_21489;
            seq_21489 = null;
            i_21492 = 0L;
            count_21491 = 0L;
            chunk_21490 = null;
            seq_21489 = ((IFn)peer$fail_pending_txes.const__15.getRawRoot()).invoke(v20);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$fail_pending_txes.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"clear");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__2 = RT.var((String)"datomic.cache", (String)"cache-keys");
        const__5 = RT.var((String)"datomic.cache", (String)"remove");
        const__6 = RT.var((String)"clojure.core", (String)"deliver");
        const__7 = RT.var((String)"datomic.peer", (String)"transactor-unavailable");
        const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__14 = RT.var((String)"clojure.core", (String)"first");
        const__15 = RT.var((String)"clojure.core", (String)"next");
    }
}

