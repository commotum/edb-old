/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.NotificationHandler;

public final class connector$fn__21042$G__20997__21045
extends AFunction {
    public Object invoke(Object gf_____21043, Object gf__db__21044) {
        Object object = gf_____21043;
        gf_____21043 = null;
        Object object2 = gf__db__21044;
        gf__db__21044 = null;
        return ((NotificationHandler)object).notify_db(object2);
    }
}

