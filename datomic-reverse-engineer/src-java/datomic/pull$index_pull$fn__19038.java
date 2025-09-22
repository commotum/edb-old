/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import datomic.pull$index_pull$fn__19038$fn__19039;

public final class pull$index_pull$fn__19038
extends AFunction {
    Object db;
    Object pull_kw;
    Object cached_selector;

    public pull$index_pull$fn__19038(Object object, Object object2, Object object3) {
        this.db = object;
        this.pull_kw = object2;
        this.cached_selector = object3;
    }

    public Object invoke(Object datom) {
        Object object = datom;
        datom = null;
        return new Delay((IFn)new pull$index_pull$fn__19038$fn__19039(object, this.db, this.pull_kw, this.cached_selector));
    }
}

