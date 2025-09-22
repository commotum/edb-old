/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.NotificationHandler;

public final class connector$fn__21003$G__20991__21006
extends AFunction {
    public Object invoke(Object gf_____21004, Object gf__id__21005) {
        Object object = gf_____21004;
        gf_____21004 = null;
        Object object2 = gf__id__21005;
        gf__id__21005 = null;
        return ((NotificationHandler)object).notify_sync(object2);
    }
}

