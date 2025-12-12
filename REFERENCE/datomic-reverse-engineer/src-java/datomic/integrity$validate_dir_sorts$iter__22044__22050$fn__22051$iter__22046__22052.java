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
import datomic.integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053;

public final class integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052
extends AFunction {
    Object tier;

    public integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052(Object object) {
        this.tier = object;
    }

    public Object invoke(Object s__22047) {
        Object object = s__22047;
        s__22047 = null;
        return new LazySeq((IFn)new integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053(this.tier, object, (Object)this));
    }
}

