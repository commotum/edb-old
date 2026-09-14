/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.connector;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.connector.NotificationHandler;
import datomic.connector.TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21255;
import datomic.connector.TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21257;
import datomic.queue.BlockingConsumer;

public final class TransactorHornetConnector$fn__21249$fn__21253$fn__21254
extends AFunction {
    Object handlers;
    Object hornet_producer;
    Object failure_handler;
    Object session;
    Object unsent_updates_queue;
    Object push_handler;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__3;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public TransactorHornetConnector$fn__21249$fn__21253$fn__21254(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.handlers = object;
        this.hornet_producer = object2;
        this.failure_handler = object3;
        this.session = object4;
        this.unsent_updates_queue = object5;
        this.push_handler = object6;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            while (true) {
                block8: {
                    block10: {
                        block9: {
                            if (Util.classOf((Object)(v0 = this.unsent_updates_queue)) == TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__cached_class__0) ** GOTO lbl7
                            if (!(v0 instanceof BlockingConsumer)) {
                                v0 = v0;
                                TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__cached_class__0 = Util.classOf((Object)v0);
lbl7:
                                // 2 sources

                                v1 = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.const__0.getRawRoot().invoke(v0);
                            } else {
                                v1 = ((BlockingConsumer)v0).take();
                            }
                            tx = v1;
                            msg = ((IFn)new TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21255(tx, this.handlers, this.session)).invoke();
                            if (!(msg instanceof Throwable)) break block8;
                            v2 = this.push_handler;
                            if (Util.classOf((Object)v2) == TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__cached_class__1) break block9;
                            if (v2 instanceof NotificationHandler) break block10;
                            v2 = v2;
                            TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__cached_class__1 = Util.classOf((Object)v2);
                        }
                        v3 = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__thunk__0__;
                        v4 = tx;
                        tx = null;
                        v5 = v3.get(v4);
                        if (v3 == v5) {
                            TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__thunk__0__ = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__site__0__.fault(v4);
                            v5 = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__thunk__0__.get(v4);
                        }
                        v6 = msg;
                        msg = null;
                        v7 = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.const__3.getRawRoot().invoke(v2, v5, v6);
                        continue;
                    }
                    v8 = (NotificationHandler)v2;
                    v9 = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__thunk__0__;
                    v10 = tx;
                    tx = null;
                    v11 = v9.get(v10);
                    if (v9 == v11) {
                        TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__thunk__0__ = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__site__0__.fault(v10);
                        v11 = TransactorHornetConnector$fn__21249$fn__21253$fn__21254.__thunk__0__.get(v10);
                    }
                    v12 = msg;
                    msg = null;
                    v7 = v8.notify_error(v11, v12);
                    continue;
                }
                v13 = msg;
                msg = null;
                send_result = ((IFn)new TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21257(this.hornet_producer, v13)).invoke();
                if (send_result instanceof Throwable) break;
            }
            v14 = send_result;
            send_result = null;
            var4_5 = ((IFn)this.failure_handler).invoke(v14);
        }
        catch (InterruptedException _) {
            var4_5 = null;
        }
        return var4_5;
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"take");
        const__3 = RT.var((String)"datomic.connector", (String)"notify-error");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
        __thunk__0__ = __site__0__;
    }
}

