/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.NotificationHandler;

public final class connector$fn__21016$G__20995__21020
extends AFunction {
    public Object invoke(Object gf_____21017, Object gf__id__21018, Object gf__error__21019) {
        Object object = gf_____21017;
        gf_____21017 = null;
        Object object2 = gf__id__21018;
        gf__id__21018 = null;
        Object object3 = gf__error__21019;
        gf__error__21019 = null;
        return ((NotificationHandler)object).notify_error(object2, object3);
    }
}

