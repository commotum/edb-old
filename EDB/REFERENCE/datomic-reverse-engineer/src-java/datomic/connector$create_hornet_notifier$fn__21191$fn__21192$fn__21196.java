/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197;

public final class connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196
extends AFunction {
    Object cleanup_ref;
    Object done_ref;
    Object push_handler_ref;
    Object hornet_consumer;
    Object failure_handler;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"threw");

    public connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.cleanup_ref = object;
        this.done_ref = object2;
        this.push_handler_ref = object3;
        this.hornet_consumer = object4;
        this.failure_handler = object5;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.cleanup_ref = null;
            this.done_ref = null;
            this.push_handler_ref = null;
            this.hornet_consumer = null;
            this.failure_handler = null;
            objectArray[1] = ((IFn)new connector$create_hornet_notifier$fn__21191$fn__21192$fn__21196$fn__21197(this.cleanup_ref, this.done_ref, this.push_handler_ref, this.hornet_consumer, this.failure_handler)).invoke();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

