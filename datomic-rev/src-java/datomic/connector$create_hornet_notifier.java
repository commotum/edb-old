/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.connector$create_hornet_notifier$fn__21191;
import datomic.connector$create_hornet_notifier$fn__21214;
import datomic.connector.HornetNotifier;
import java.lang.ref.WeakReference;

public final class connector$create_hornet_notifier
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"promise");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"runonce");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deliver");

    public static Object invokeStatic(Object push_handler, Object session, Object result_queue, Object hornet_consumer, Object failure_handler) {
        Object object = push_handler;
        push_handler = null;
        WeakReference<Object> push_handler_ref = new WeakReference<Object>(object);
        Object cleanup_ref = ((IFn)const__0.getRawRoot()).invoke();
        Object done_ref = ((IFn)const__0.getRawRoot()).invoke();
        Object object2 = failure_handler;
        failure_handler = null;
        Delay starter = new Delay((IFn)new connector$create_hornet_notifier$fn__21191(cleanup_ref, done_ref, push_handler_ref, hornet_consumer, object2));
        Object object3 = done_ref;
        done_ref = null;
        Object cleanup2 = ((IFn)const__1.getRawRoot()).invoke((Object)new connector$create_hornet_notifier$fn__21214(session, object3, hornet_consumer, result_queue));
        Object object4 = cleanup_ref;
        cleanup_ref = null;
        ((IFn)const__2.getRawRoot()).invoke(object4, cleanup2);
        WeakReference<Object> weakReference = push_handler_ref;
        push_handler_ref = null;
        Object object5 = session;
        session = null;
        Object object6 = result_queue;
        result_queue = null;
        Object object7 = hornet_consumer;
        hornet_consumer = null;
        Delay delay = starter;
        starter = null;
        Object object8 = cleanup2;
        cleanup2 = null;
        return new HornetNotifier(weakReference, object5, object6, object7, delay, object8);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return connector$create_hornet_notifier.invokeStatic(object6, object7, object8, object9, object10);
    }
}

