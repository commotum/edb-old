/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.spi.Scan;

public final class spi$fn__20933$G__20929__20936
extends AFunction {
    public Object invoke(Object gf_____20934, Object gf__opts__20935) {
        Object object = gf_____20934;
        gf_____20934 = null;
        Object object2 = gf__opts__20935;
        gf__opts__20935 = null;
        return ((Scan)object)._scan(object2);
    }
}

