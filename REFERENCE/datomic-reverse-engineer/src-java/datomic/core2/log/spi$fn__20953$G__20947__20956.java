/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.spi.Item;

public final class spi$fn__20953$G__20947__20956
extends AFunction {
    public Object invoke(Object gf_____20954, Object gf__item__20955) {
        Object object = gf_____20954;
        gf_____20954 = null;
        Object object2 = gf__item__20955;
        gf__item__20955 = null;
        return ((Item)object)._item_header(object2);
    }
}

