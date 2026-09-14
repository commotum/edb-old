/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import datomic.core2.atom.spi.DurableAtom;

public final class spi$fn__20414$G__20395__20417
extends AFunction {
    public Object invoke(Object gf_____20415, Object gf__ch__20416) {
        Object object = gf_____20415;
        gf_____20415 = null;
        Object object2 = gf__ch__20416;
        gf__ch__20416 = null;
        return ((DurableAtom)object)._sync(object2);
    }
}

