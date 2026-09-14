/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.reconnector2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Reconnector$fn__17136
extends AFunction {
    Object current_promise_ref;
    Object worker_ref;
    Object reconnect_fn;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"realized?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__5 = RT.var((String)"datomic.monitor", (String)"alarm");
    public static final Keyword const__6 = RT.keyword(null, (String)"UnhandledException");

    public Reconnector$fn__17136(Object object, Object object2, Object object3) {
        this.current_promise_ref = object;
        this.worker_ref = object2;
        this.reconnect_fn = object3;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            Object state2 = ((IFn)this.reconnect_fn).invoke();
            Object lockee__5436__auto__17138 = this.worker_ref;
            try {
                synchronized (lockee__5436__auto__17138) {
                    Object object3;
                    ((IFn)const__0.getRawRoot()).invoke(this.worker_ref, null);
                    Object object4 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.current_promise_ref));
                    if (object4 != null && object4 != Boolean.FALSE) {
                        object3 = null;
                    } else {
                        Object object5 = state2;
                        state2 = null;
                        object3 = ((IFn)((IFn)const__2.getRawRoot()).invoke(this.current_promise_ref)).invoke(object5);
                    }
                    object2 = object3;
                }
            }
            finally {
                Object object6 = lockee__5436__auto__17138;
                lockee__5436__auto__17138 = null;
                // ** MonitorExit[v3] (shouldn't be in output)
            }
            {
                object = object2;
            }
        }
        catch (Throwable t__9147__auto__2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.reconnector2");
            Throwable ex = t__9147__auto__2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__3.getRawRoot()).invoke((Object)"error executing future"), ex);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__4.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            ((IFn)const__5.getRawRoot()).invoke((Object)const__6);
            Object t__9147__auto__2 = null;
            throw t__9147__auto__2;
        }
        return object;
    }
}

