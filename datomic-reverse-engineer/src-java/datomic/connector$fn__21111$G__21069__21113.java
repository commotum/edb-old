/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.connector.TransactorConnector;

public final class connector$fn__21111$G__21069__21113
extends AFunction {
    public Object invoke(Object gf_____21112) {
        Object object = gf_____21112;
        gf_____21112 = null;
        return ((TransactorConnector)object).endpoint();
    }
}

