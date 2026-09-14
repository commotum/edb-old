/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  com.datomic.lucene.index.TermEnum
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import com.datomic.lucene.index.TermEnum;
import datomic.lucene$term_enum_seq$fn__12313;

public final class lucene$term_enum_seq
extends AFunction {
    public static Object invokeStatic(Object te) {
        LazySeq lazySeq;
        if (((TermEnum)te).next()) {
            te = null;
            lazySeq = new LazySeq((IFn)new lucene$term_enum_seq$fn__12313(te));
        } else {
            lazySeq = null;
        }
        return lazySeq;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$term_enum_seq.invokeStatic(object2);
    }
}

