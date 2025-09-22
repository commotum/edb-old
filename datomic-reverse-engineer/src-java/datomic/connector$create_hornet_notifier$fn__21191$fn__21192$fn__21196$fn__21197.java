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
import datomic.queue.BlockingConsumer;
import java.lang.ref.Reference;

public final class connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197
extends AFunction {
    Object cleanup_ref;
    Object done_ref;
    Object push_handler_ref;
    Object hornet_consumer;
    Object failure_handler;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__8;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__19;

    public connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.cleanup_ref = object;
        this.done_ref = object2;
        this.push_handler_ref = object3;
        this.hornet_consumer = object4;
        this.failure_handler = object5;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            block18: {
                block16: {
                    v0 = this.hornet_consumer;
                    if (Util.classOf((Object)v0) == connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.__cached_class__0) ** GOTO lbl7
                    if (!(v0 instanceof BlockingConsumer)) {
                        v0 = v0;
                        connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.__cached_class__0 = Util.classOf((Object)v0);
lbl7:
                        // 2 sources

                        v1 = connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__0.getRawRoot().invoke(v0);
                    } else {
                        v1 = ((BlockingConsumer)v0).take();
                    }
                    msges = v1;
                    while (true) {
                        v2 = and__5236__auto__21203 = msges;
                        if (v2 != null && v2 != Boolean.FALSE) {
                            v3 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__1.getRawRoot()).invoke(((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__2.getRawRoot()).invoke(this.done_ref));
                        } else {
                            v3 = and__5236__auto__21203;
                            and__5236__auto__21203 = null;
                        }
                        if (v3 == null || v3 == Boolean.FALSE) break block16;
                        v4 = temp__5457__auto__21206 = ((Reference)this.push_handler_ref).get();
                        if (v4 == null || v4 == Boolean.FALSE) break;
                        v5 = temp__5457__auto__21206;
                        temp__5457__auto__21206 = null;
                        push_handler = v5;
                        v6 = msges;
                        msges = null;
                        seq_21198 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__3.getRawRoot()).invoke(((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__4.getRawRoot()).invoke(v6, connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__5.getRawRoot()));
                        chunk_21199 = null;
                        count_21200 = 0L;
                        i_21201 = 0L;
                        while (true) {
                            if (i_21201 < count_21200) {
                                v7 = msg = ((Indexed)chunk_21199).nth(RT.intCast((long)i_21201));
                                msg = null;
                                ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__8.getRawRoot()).invoke(v7, push_handler);
                                v8 = seq_21198;
                                seq_21198 = null;
                                v9 = chunk_21199;
                                chunk_21199 = null;
                                ++i_21201;
                                chunk_21199 = v9;
                                seq_21198 = v8;
                                continue;
                            }
                            v10 = seq_21198;
                            seq_21198 = null;
                            v11 = temp__5457__auto__21205 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__3.getRawRoot()).invoke(v10);
                            if (v11 == null || v11 == Boolean.FALSE) break;
                            v12 = temp__5457__auto__21205;
                            temp__5457__auto__21205 = null;
                            seq_21198 = v12;
                            v13 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__10.getRawRoot()).invoke(seq_21198);
                            if (v13 != null && v13 != Boolean.FALSE) {
                                c__5719__auto__21204 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__11.getRawRoot()).invoke(seq_21198);
                                v14 = seq_21198;
                                seq_21198 = null;
                                v15 = c__5719__auto__21204;
                                v16 = c__5719__auto__21204;
                                c__5719__auto__21204 = null;
                                i_21201 = RT.intCast((long)0L);
                                count_21200 = RT.intCast((int)RT.count((Object)v16));
                                chunk_21199 = v15;
                                seq_21198 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__12.getRawRoot()).invoke(v14);
                                continue;
                            }
                            v17 = msg = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__15.getRawRoot()).invoke(seq_21198);
                            msg = null;
                            ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__8.getRawRoot()).invoke(v17, push_handler);
                            v18 = seq_21198;
                            seq_21198 = null;
                            i_21201 = 0L;
                            count_21200 = 0L;
                            chunk_21199 = null;
                            seq_21198 = ((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__16.getRawRoot()).invoke(v18);
                        }
                        v19 = this.hornet_consumer;
                        if (Util.classOf((Object)v19) == connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.__cached_class__1) ** GOTO lbl81
                        if (!(v19 instanceof BlockingConsumer)) {
                            v19 = v19;
                            connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.__cached_class__1 = Util.classOf((Object)v19);
lbl81:
                            // 2 sources

                            v20 = connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__0.getRawRoot().invoke(v19);
                        } else {
                            v20 = ((BlockingConsumer)v19).take();
                        }
                        msges = v20;
                    }
                    v21 = null;
                    break block18;
                }
                v21 = null;
            }
            var13_13 = v21;
        }
        catch (Throwable t) {
            if (!(t instanceof InterruptedException)) {
                this.failure_handler = null;
                ((IFn)this.failure_handler).invoke((Object)t);
                t = null;
                throw t;
            }
            var13_13 = null;
        }
        finally {
            this.cleanup_ref = null;
            ((IFn)((IFn)connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197.const__19.getRawRoot()).invoke(this.cleanup_ref)).invoke();
        }
        return var13_13;
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"take");
        const__1 = RT.var((String)"clojure.core", (String)"not");
        const__2 = RT.var((String)"clojure.core", (String)"realized?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.var((String)"datomic.artemis-client", (String)"read-batch");
        const__5 = RT.var((String)"datomic.transaction", (String)"read-handlers");
        const__8 = RT.var((String)"datomic.connector", (String)"notify");
        const__10 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__11 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__12 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__15 = RT.var((String)"clojure.core", (String)"first");
        const__16 = RT.var((String)"clojure.core", (String)"next");
        const__19 = RT.var((String)"clojure.core", (String)"deref");
    }
}

