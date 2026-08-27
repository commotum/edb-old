/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Iterator;

public final class peer$seq_iterable$reify__21673
implements Iterable,
IObj {
    final IPersistentMap __meta;
    Object aseq;
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"seq-iterator");

    public peer$seq_iterable$reify__21673(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.aseq = object;
    }

    public peer$seq_iterable$reify__21673(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new peer$seq_iterable$reify__21673(iPersistentMap, this.aseq);
    }

    public Iterator iterator() {
        peer$seq_iterable$reify__21673 this_ = null;
        return (Iterator)((IFn)const__0.getRawRoot()).invoke(this_.aseq);
    }
}

