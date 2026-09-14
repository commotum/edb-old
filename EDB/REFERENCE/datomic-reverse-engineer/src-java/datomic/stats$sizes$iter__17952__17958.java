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
import datomic.stats$sizes$iter__17952__17958$fn__17959;

public final class stats$sizes$iter__17952__17958
extends AFunction {
    Object db;
    Object with_key_summary;

    public stats$sizes$iter__17952__17958(Object object, Object object2) {
        this.db = object;
        this.with_key_summary = object2;
    }

    public Object invoke(Object s__17953) {
        Object object = s__17953;
        s__17953 = null;
        return new LazySeq((IFn)new stats$sizes$iter__17952__17958$fn__17959((Object)this, object, this.db, this.with_key_summary));
    }
}

