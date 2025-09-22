/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.spi.Delete;

public final class spi$fn__20915$G__20911__20918
extends AFunction {
    public Object invoke(Object gf_____20916, Object gf__t__20917) {
        Object object = gf_____20916;
        gf_____20916 = null;
        Object object2 = gf__t__20917;
        gf__t__20917 = null;
        return ((Delete)object)._delete(object2);
    }
}

