/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.spi.Item;

public final class spi$fn__20966$G__20949__20969
extends AFunction {
    public Object invoke(Object gf_____20967, Object gf__item__20968) {
        Object object = gf_____20967;
        gf_____20967 = null;
        Object object2 = gf__item__20968;
        gf__item__20968 = null;
        return ((Item)object)._item_body(object2);
    }
}

