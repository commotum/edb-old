/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
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

public final class IterCat
implements Iter,
IType {
    Object iter;
    Object iters;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");

    public IterCat(Object object, Object object2) {
        this.iter = object;
        this.iters = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), (Object)((IObj)Symbol.intern(null, (String)"iters")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        IterCat iterCat;
        Object temp__5455__auto__11757;
        Object object = temp__5455__auto__11757 = ((Iter)this.iter).next();
        if (object != null && object != Boolean.FALSE) {
            Object nxt;
            Object object2 = temp__5455__auto__11757;
            temp__5455__auto__11757 = null;
            Object object3 = nxt = object2;
            nxt = null;
            this.iter = object3;
            iterCat = this;
        } else {
            Object vec__11753;
            Object object4 = vec__11753 = this.iters;
            vec__11753 = null;
            Object seq__11754 = ((IFn)const__0.getRawRoot()).invoke(object4);
            Object first__11755 = ((IFn)const__1.getRawRoot()).invoke(seq__11754);
            Object object5 = seq__11754;
            seq__11754 = null;
            Object seq__117542 = ((IFn)const__2.getRawRoot()).invoke(object5);
            Object object6 = first__11755;
            first__11755 = null;
            Object nxt = object6;
            Object object7 = seq__117542;
            seq__117542 = null;
            Object more = object7;
            Object object8 = nxt;
            if (object8 != null && object8 != Boolean.FALSE) {
                Object object9 = nxt;
                nxt = null;
                this.iter = object9;
                Object object10 = more;
                more = null;
                this.iters = object10;
                iterCat = this;
            } else {
                iterCat = null;
            }
        }
        return iterCat;
    }

    public Object get() {
        return ((Iter)this.iter).get();
    }
}

