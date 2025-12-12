/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.log;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.iter.Iter;
import datomic.log.LogDirSeq;
import datomic.log.LogSegSeq;

public final class TailTxIter
implements LogDirSeq,
Iter,
LogSegSeq,
IType {
    public final Object txes;
    long idx;

    public TailTxIter(Object object, long l) {
        this.txes = object;
        this.idx = l;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"txes"), (Object)((IObj)Symbol.intern(null, (String)"idx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        TailTxIter tailTxIter;
        if (Numbers.inc((long)this.idx) < (long)RT.count((Object)this.txes)) {
            this.idx = Numbers.inc((long)this.idx);
            tailTxIter = this;
        } else {
            tailTxIter = null;
        }
        return tailTxIter;
    }

    public Object get() {
        TailTxIter this_ = null;
        return RT.nth((Object)this_.txes, (int)RT.intCast((long)this_.idx));
    }

    public Object log_dir_seq() {
        return null;
    }

    public Object log_seg_seq() {
        return null;
    }
}

