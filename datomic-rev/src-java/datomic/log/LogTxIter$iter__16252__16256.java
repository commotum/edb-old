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
import datomic.log.LogTxIter$iter__16252__16256$fn__16257;

public final class LogTxIter$iter__16252__16256
extends AFunction {
    Object root_val;
    Object lookup;

    public LogTxIter$iter__16252__16256(Object object, Object object2) {
        this.root_val = object;
        this.lookup = object2;
    }

    public Object invoke(Object s__16253) {
        Object object = s__16253;
        s__16253 = null;
        return new LazySeq((IFn)new LogTxIter$iter__16252__16256$fn__16257((Object)this, this.root_val, this.lookup, object));
    }
}

