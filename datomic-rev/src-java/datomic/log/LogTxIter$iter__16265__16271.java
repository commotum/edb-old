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
import datomic.log.LogTxIter$iter__16265__16271$fn__16272;

public final class LogTxIter$iter__16265__16271
extends AFunction {
    long didx;
    Object root_val;
    Object lookup;

    public LogTxIter$iter__16265__16271(long l, Object object, Object object2) {
        this.didx = l;
        this.root_val = object;
        this.lookup = object2;
    }

    public Object invoke(Object s__16266) {
        Object object = s__16266;
        s__16266 = null;
        return new LazySeq((IFn)new LogTxIter$iter__16265__16271$fn__16272(this.didx, object, this.root_val, (Object)this, this.lookup));
    }
}

