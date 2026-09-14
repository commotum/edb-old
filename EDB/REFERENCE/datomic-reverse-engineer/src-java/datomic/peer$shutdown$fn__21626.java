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
import datomic.common.AsyncShutdown;

public final class peer$shutdown$fn__21626
extends AFunction {
    Object lockee__5436__auto__;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
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

    public peer$shutdown$fn__21626(Object object) {
        this.lockee__5436__auto__ = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            synchronized (this.lockee__5436__auto__) {
                seq_21627 = ((IFn)peer$shutdown$fn__21626.const__0.getRawRoot()).invoke(((IFn)peer$shutdown$fn__21626.const__1.getRawRoot()).invoke(peer$shutdown$fn__21626.const__2.getRawRoot()));
                chunk_21628 = null;
                count_21629 = 0L;
                i_21630 = 0L;
                while (true) {
                    block13: {
                        block11: {
                            block12: {
                                if (i_21630 >= count_21629) break block11;
                                v0 = k = ((Indexed)chunk_21628).nth(RT.intCast((long)i_21630));
                                k = null;
                                v1 = temp__5457__auto__21632 = ((IFn)peer$shutdown$fn__21626.const__5.getRawRoot()).invoke(peer$shutdown$fn__21626.const__2.getRawRoot(), v0);
                                if (v1 == null || v1 == Boolean.FALSE) break block12;
                                v2 = temp__5457__auto__21632;
                                temp__5457__auto__21632 = null;
                                conn = v2;
                                v3 = (IFn)peer$shutdown$fn__21626.const__6.getRawRoot();
                                v4 = conn;
                                conn = null;
                                v5 = v4;
                                if (Util.classOf((Object)v4) == peer$shutdown$fn__21626.__cached_class__0) ** GOTO lbl25
                                if (!(v5 instanceof AsyncShutdown)) {
                                    v5 = v5;
                                    peer$shutdown$fn__21626.__cached_class__0 = Util.classOf((Object)v5);
lbl25:
                                    // 2 sources

                                    v6 = peer$shutdown$fn__21626.const__7.getRawRoot().invoke(v5);
                                } else {
                                    v6 = ((AsyncShutdown)v5).async_shutdown();
                                }
                                v3.invoke(v6);
                            }
                            v7 = seq_21627;
                            seq_21627 = null;
                            v8 = chunk_21628;
                            chunk_21628 = null;
                            ++i_21630;
                            chunk_21628 = v8;
                            seq_21627 = v7;
                            continue;
                        }
                        v9 = seq_21627;
                        seq_21627 = null;
                        v10 = temp__5457__auto__21635 = ((IFn)peer$shutdown$fn__21626.const__0.getRawRoot()).invoke(v9);
                        if (v10 == null || v10 == Boolean.FALSE) break;
                        v11 = temp__5457__auto__21635;
                        temp__5457__auto__21635 = null;
                        seq_21627 = v11;
                        v12 = ((IFn)peer$shutdown$fn__21626.const__9.getRawRoot()).invoke(seq_21627);
                        if (v12 != null && v12 != Boolean.FALSE) {
                            c__5719__auto__21633 = ((IFn)peer$shutdown$fn__21626.const__10.getRawRoot()).invoke(seq_21627);
                            v13 = seq_21627;
                            seq_21627 = null;
                            v14 = c__5719__auto__21633;
                            v15 = c__5719__auto__21633;
                            c__5719__auto__21633 = null;
                            i_21630 = RT.intCast((long)0L);
                            count_21629 = RT.intCast((int)RT.count((Object)v15));
                            chunk_21628 = v14;
                            seq_21627 = ((IFn)peer$shutdown$fn__21626.const__11.getRawRoot()).invoke(v13);
                            continue;
                        }
                        v16 = k = ((IFn)peer$shutdown$fn__21626.const__14.getRawRoot()).invoke(seq_21627);
                        k = null;
                        v17 = temp__5457__auto__21634 = ((IFn)peer$shutdown$fn__21626.const__5.getRawRoot()).invoke(peer$shutdown$fn__21626.const__2.getRawRoot(), v16);
                        if (v17 == null || v17 == Boolean.FALSE) break block13;
                        v18 = temp__5457__auto__21634;
                        temp__5457__auto__21634 = null;
                        conn = v18;
                        v19 = (IFn)peer$shutdown$fn__21626.const__6.getRawRoot();
                        v20 = conn;
                        conn = null;
                        v21 = v20;
                        if (Util.classOf((Object)v20) == peer$shutdown$fn__21626.__cached_class__1) ** GOTO lbl75
                        if (!(v21 instanceof AsyncShutdown)) {
                            v21 = v21;
                            peer$shutdown$fn__21626.__cached_class__1 = Util.classOf((Object)v21);
lbl75:
                            // 2 sources

                            v22 = peer$shutdown$fn__21626.const__7.getRawRoot().invoke(v21);
                        } else {
                            v22 = ((AsyncShutdown)v21).async_shutdown();
                        }
                        v19.invoke(v22);
                    }
                    v23 = seq_21627;
                    seq_21627 = null;
                    i_21630 = 0L;
                    count_21629 = 0L;
                    chunk_21628 = null;
                    seq_21627 = ((IFn)peer$shutdown$fn__21626.const__15.getRawRoot()).invoke(v23);
                }
                var12_10 = null;
            }
        }
        finally {
            this.lockee__5436__auto__ = null;
            // ** MonitorExit[this.lockee__5436__auto__] (shouldn't be in output)
        }
        {
            return var12_10;
        }
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq");
        const__1 = RT.var((String)"datomic.cache", (String)"cache-keys");
        const__2 = RT.var((String)"datomic.peer", (String)"connection-cache");
        const__5 = RT.var((String)"datomic.cache", (String)"remove");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__7 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__14 = RT.var((String)"clojure.core", (String)"first");
        const__15 = RT.var((String)"clojure.core", (String)"next");
    }
}

