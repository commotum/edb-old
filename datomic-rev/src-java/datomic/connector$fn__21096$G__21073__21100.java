/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.TransactorConnector;

public final class connector$fn__21096$G__21073__21100
extends AFunction {
    public Object invoke(Object gf_____21097, Object gf__handler__21098, Object gf__failure_handler__21099) {
        Object object = gf_____21097;
        gf_____21097 = null;
        Object object2 = gf__handler__21098;
        gf__handler__21098 = null;
        Object object3 = gf__failure_handler__21099;
        gf__failure_handler__21099 = null;
        return ((TransactorConnector)object).create_notifier(object2, object3);
    }
}

