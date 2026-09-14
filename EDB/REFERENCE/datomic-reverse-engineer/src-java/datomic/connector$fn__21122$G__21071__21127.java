/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.TransactorConnector;

public final class connector$fn__21122$G__21071__21127
extends AFunction {
    public Object invoke(Object gf_____21123, Object gf__request__21124, Object gf__arg__21125, Object gf__timeout_msec__21126) {
        Object object = gf_____21123;
        gf_____21123 = null;
        Object object2 = gf__request__21124;
        gf__request__21124 = null;
        Object object3 = gf__arg__21125;
        gf__arg__21125 = null;
        Object object4 = gf__timeout_msec__21126;
        gf__timeout_msec__21126 = null;
        return ((TransactorConnector)object).admin_request_STAR_(object2, object3, object4);
    }
}

