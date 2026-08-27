/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class pull$limit_iterable$reify$reify__18899
implements Iterator,
IObj {
    final IPersistentMap __meta;
    Object iter;
    Object i;
    Object limit;

    public pull$limit_iterable$reify$reify__18899(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.iter = object;
        this.i = object2;
        this.limit = object3;
    }

    public pull$limit_iterable$reify$reify__18899(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new pull$limit_iterable$reify$reify__18899(iPersistentMap, this.iter, this.i, this.limit);
    }

    public Object next() {
        long _i = ((long[])this.i)[RT.intCast((long)0L)];
        if (!Numbers.lt((long)_i, (Object)this.limit)) {
            throw (Throwable)new NoSuchElementException();
        }
        RT.aset((long[])((long[])this.i), (int)RT.intCast((long)0L), (long)Numbers.inc((long)_i));
        return ((Iterator)this.iter).next();
    }

    /*
     * WARNING - void declaration
     */
    public boolean hasNext() {
        boolean bl;
        boolean and__5236__auto__18901 = Numbers.lt((long)((long[])this_.i)[RT.intCast((long)0L)], (Object)this_.limit);
        if (and__5236__auto__18901) {
            pull$limit_iterable$reify$reify__18899 this_ = null;
            bl = ((Iterator)this_.iter).hasNext();
        } else {
            void var1_1;
            bl = var1_1;
        }
        return bl;
    }
}

