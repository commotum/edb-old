/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.log.LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274;

public final class LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273
extends AFunction {
    Object lookup;
    Object dir;

    public LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273(Object object, Object object2) {
        this.lookup = object;
        this.dir = object2;
    }

    public Object invoke(Object s__16268) {
        Object object = s__16268;
        s__16268 = null;
        return new LazySeq((IFn)new LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274(this.lookup, (Object)this, object, this.dir));
    }
}

