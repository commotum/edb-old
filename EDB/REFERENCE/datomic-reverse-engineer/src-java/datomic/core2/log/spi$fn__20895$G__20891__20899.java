/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.spi.Append;

public final class spi$fn__20895$G__20891__20899
extends AFunction {
    public Object invoke(Object gf_____20896, Object gf__header__20897, Object gf__body__20898) {
        Object object = gf_____20896;
        gf_____20896 = null;
        Object object2 = gf__header__20897;
        gf__header__20897 = null;
        Object object3 = gf__body__20898;
        gf__body__20898 = null;
        return ((Append)object)._append(object2, object3);
    }
}

