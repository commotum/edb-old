/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import datomic.core2.val_store.s3.sdkv1.Impl;

public final class sdkv1$fn__21689$G__21679__21693
extends AFunction {
    public Object invoke(Object gf_____21690, Object gf__k__21691, Object gf__opts__21692) {
        Object object = gf_____21690;
        gf_____21690 = null;
        Object object2 = gf__k__21691;
        gf__k__21691 = null;
        Object object3 = gf__opts__21692;
        gf__opts__21692 = null;
        return ((Impl)object)._sync_get(object2, object3);
    }
}

