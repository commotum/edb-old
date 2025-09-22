/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.iter;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.iter.Iter;

public final class MergeIter
implements Iter,
IType {
    public final Object cmp;
    public final Object iters;
    long i;
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"least-index");

    public MergeIter(Object object, Object object2, long l) {
        this.cmp = object;
        this.iters = object2;
        this.i = l;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Comparator")})), (Object)((IObj)Symbol.intern(null, (String)"iters")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})), (Object)((IObj)Symbol.intern(null, (String)"i")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        Object object;
        Object nxt;
        Object object2 = nxt = ((Iter)RT.aget((Object[])((Object[])this_.iters), (int)RT.intCast((long)this_.i))).next();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = nxt;
            nxt = null;
            RT.aset((Object[])((Object[])this_.iters), (int)RT.intCast((long)this_.i), (Object)object3);
            this_.i = ((IFn.OOL)const__3.getRawRoot()).invokePrim(this_.cmp, this_.iters);
            object = this_;
        } else {
            MergeIter this_ = null;
            object = RT.aget((Object[])((Object[])this_.iters), (int)RT.intCast((long)(this_.i == 0L ? 1L : 0L)));
        }
        return object;
    }

    public Object get() {
        return ((Iter)RT.aget((Object[])((Object[])this.iters), (int)RT.intCast((long)this.i))).get();
    }
}

