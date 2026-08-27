/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.NotificationHandler;

public final class connector$fn__21055$G__20993__21058
extends AFunction {
    public Object invoke(Object gf_____21056, Object gf__msg__21057) {
        Object object = gf_____21056;
        gf_____21056 = null;
        Object object2 = gf__msg__21057;
        gf__msg__21057 = null;
        return ((NotificationHandler)object).notify_data(object2);
    }
}

