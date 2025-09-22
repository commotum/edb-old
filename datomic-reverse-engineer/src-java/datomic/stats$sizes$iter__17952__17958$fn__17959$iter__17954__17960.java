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
import datomic.stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961;

public final class stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960
extends AFunction {
    Object db;
    Object index;
    Object with_key_summary;

    public stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960(Object object, Object object2, Object object3) {
        this.db = object;
        this.index = object2;
        this.with_key_summary = object3;
    }

    public Object invoke(Object s__17955) {
        Object object = s__17955;
        s__17955 = null;
        return new LazySeq((IFn)new stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961((Object)this, this.db, object, this.index, this.with_key_summary));
    }
}

