/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import datomic.core2.val_store.s3.sdkv1.Impl;

public final class sdkv1$fn__21704$G__21681__21709
extends AFunction {
    public Object invoke(Object gf_____21705, Object gf__k__21706, Object gf__v__21707, Object gf__opts__21708) {
        Object object = gf_____21705;
        gf_____21705 = null;
        Object object2 = gf__k__21706;
        gf__k__21706 = null;
        Object object3 = gf__v__21707;
        gf__v__21707 = null;
        Object object4 = gf__opts__21708;
        gf__opts__21708 = null;
        return ((Impl)object)._sync_put(object2, object3, object4);
    }
}

