/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.pull;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.iter.Iter;

public final class AIter
implements Iter,
IType {
    public final Object db;
    public final long e;
    long a;
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"next-a");

    public AIter(Object object, long l, long l2) {
        this.db = object;
        this.e = l;
        this.a = l2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)((IObj)Symbol.intern(null, (String)"e")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"a")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        AIter aIter;
        Object temp__5457__auto__18989;
        Object object = temp__5457__auto__18989 = ((IFn)const__0.getRawRoot()).invoke(this.db, (Object)Numbers.num((long)this.e), (Object)Numbers.num((long)this.a));
        if (object != null && object != Boolean.FALSE) {
            Object na;
            Object object2 = temp__5457__auto__18989;
            temp__5457__auto__18989 = null;
            Object object3 = na = object2;
            na = null;
            this.a = RT.longCast((Object)object3);
            aIter = this;
        } else {
            aIter = null;
        }
        return aIter;
    }

    public Object get() {
        return Numbers.num((long)this.a);
    }
}

