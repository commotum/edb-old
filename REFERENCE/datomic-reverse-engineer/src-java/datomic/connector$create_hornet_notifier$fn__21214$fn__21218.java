/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class connector$create_hornet_notifier$fn__21214$fn__21218
extends AFunction {
    Object session;
    Object done_ref;
    Object hornet_consumer;
    Object result_queue;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"sync-shutdown");
    public static final Var const__3 = RT.var((String)"datomic.artemis-client", (String)"delete-queue");
    public static final Keyword const__4 = RT.keyword(null, (String)"threw");

    public connector$create_hornet_notifier$fn__21214$fn__21218(Object object, Object object2, Object object3, Object object4) {
        this.session = object;
        this.done_ref = object2;
        this.hornet_consumer = object3;
        this.result_queue = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.done_ref = null;
            ((IFn)const__1.getRawRoot()).invoke(this.done_ref, (Object)Boolean.TRUE);
            this.hornet_consumer = null;
            ((IFn)const__2.getRawRoot()).invoke(this.hornet_consumer);
            this.result_queue = null;
            ((IFn)const__3.getRawRoot()).invoke(this.session, this.result_queue);
            this.session = null;
            objectArray[1] = ((IFn)const__2.getRawRoot()).invoke(this.session);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

