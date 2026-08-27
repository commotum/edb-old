/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import datomic.core2.val_store.s3.sdkv1.Impl;

public final class sdkv1$fn__21742$G__21683__21746
extends AFunction {
    public Object invoke(Object gf_____21743, Object gf__k__21744, Object gf__opts__21745) {
        Object object = gf_____21743;
        gf_____21743 = null;
        Object object2 = gf__k__21744;
        gf__k__21744 = null;
        Object object3 = gf__opts__21745;
        gf__opts__21745 = null;
        return ((Impl)object)._sync_delete(object2, object3);
    }
}

