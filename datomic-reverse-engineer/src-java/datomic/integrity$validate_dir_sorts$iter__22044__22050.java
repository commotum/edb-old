/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.integrity$validate_dir_sorts$iter__22044__22050$fn__22051;

public final class integrity$validate_dir_sorts$iter__22044__22050
extends AFunction {
    public Object invoke(Object s__22045) {
        Object object = s__22045;
        s__22045 = null;
        return new LazySeq((IFn)new integrity$validate_dir_sorts$iter__22044__22050$fn__22051(object, (Object)this));
    }
}

