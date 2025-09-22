/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentSet
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Reversible
 *  clojure.lang.Seqable
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.btset;

import clojure.lang.Counted;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Reversible;
import clojure.lang.Seqable;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.BTSetBranch;
import datomic.btset.BTSetLeaf;
import datomic.btset.BTSetSplit;
import datomic.btset.IBTSet;
import datomic.btset.IBTSetNode;
import datomic.btset.IDataSet;
import datomic.iter.Iter;

public final class BTSet
implements IBTSet,
IPersistentCollection,
IPersistentSet,
Reversible,
Counted,
IDataSet,
Seqable,
IType {
    public final Object cmp;
    public final long cnt;
    public final Object root;
    public static final Keyword const__8 = RT.keyword(null, (String)"else");
    public static final Var const__11 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__12 = RT.var((String)"datomic.iter", (String)"iter-rseq");
    public static final Var const__14 = RT.var((String)"datomic.btset", (String)"comp");

    public BTSet(Object object, long l, Object object2) {
        this.cmp = object;
        this.cnt = l;
        this.root = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cmp"), (Object)((IObj)Symbol.intern(null, (String)"cnt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"root")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IBTSetNode")})));
    }

    public Iter rseek(Object k) {
        Object object;
        Iter ret = ((IDataSet)this).seek(k);
        if (Util.identical((Object)ret, null)) {
            object = ((IBTSet)this).rseek();
        } else {
            Object object2 = k;
            k = null;
            if (((IFn.OOOL)const__14.getRawRoot()).invokePrim(this.cmp, object2, ret.get()) < 0L) {
                Iter iter2 = ret;
                ret = null;
                object = iter2.prev();
            } else {
                Keyword keyword = const__8;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = ret;
                    ret = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Iter rseek() {
        Object object = this.root;
        return object != null && object != Boolean.FALSE ? ((IBTSetNode)this.root).rseek(null) : null;
    }

    public Iter seekLast() {
        return ((IBTSet)this).rseek();
    }

    public Iter seek(Object k) {
        Iter iter2;
        Object object = this.root;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = k;
            k = null;
            iter2 = ((IBTSetNode)this.root).seek(object2, null);
        } else {
            iter2 = null;
        }
        return iter2;
    }

    public Iter seek() {
        Object object = this.root;
        return object != null && object != Boolean.FALSE ? ((IBTSetNode)this.root).seek(null) : null;
    }

    public long longCount() {
        return this.cnt;
    }

    public ISeq rseq() {
        Iter iter2 = ((IBTSet)this_).rseek();
        BTSet this_ = null;
        return (ISeq)((IFn)const__12.getRawRoot()).invoke((Object)iter2);
    }

    public ISeq seq() {
        Iter iter2 = ((IDataSet)this_).seek();
        BTSet this_ = null;
        return (ISeq)((IFn)const__11.getRawRoot()).invoke((Object)iter2);
    }

    public int count() {
        return RT.intCast((long)this.cnt);
    }

    public Object get(Object k) {
        Object object;
        Iter temp__5457__auto__11850;
        Iter iter2 = temp__5457__auto__11850 = ((IDataSet)this).seek(k);
        if (iter2 != null && iter2 != Boolean.FALSE) {
            Iter iter3 = temp__5457__auto__11850;
            temp__5457__auto__11850 = null;
            Iter i = iter3;
            Object object2 = k;
            k = null;
            if (Util.equiv((Object)object2, (Object)i.get())) {
                Iter iter4 = i;
                i = null;
                object = iter4.get();
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public boolean contains(Object k) {
        Boolean bl;
        Iter temp__5457__auto__11851;
        Iter iter2 = temp__5457__auto__11851 = ((IDataSet)this).seek(k);
        if (iter2 != null && iter2 != Boolean.FALSE) {
            Iter iter3 = temp__5457__auto__11851;
            temp__5457__auto__11851 = null;
            Iter i = iter3;
            Object object = k;
            k = null;
            Iter iter4 = i;
            i = null;
            bl = Util.equiv((Object)object, (Object)iter4.get()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = null;
        }
        return RT.booleanCast(bl);
    }

    public IPersistentSet disjoin(Object k) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public boolean equiv(Object x) {
        return Boolean.FALSE;
    }

    public IPersistentCollection cons(Object k) {
        BTSet bTSet;
        if (Util.identical((Object)this.root, null)) {
            k = null;
            bTSet = new BTSet(this.cmp, 1L, new BTSetLeaf(1L, this.cmp, RT.object_array((Object)Tuple.create((Object)k))));
        } else {
            Object object = k;
            k = null;
            Object new_root = ((IBTSetNode)this.root).conjoin(object);
            if (Util.identical((Object)this.root, (Object)new_root)) {
                bTSet = this;
            } else if (new_root instanceof BTSetSplit) {
                Object object2 = new_root;
                new_root = null;
                Object split = object2;
                split = null;
                bTSet = new BTSet(this.cmp, this.cnt + 1L, new BTSetBranch(this.cmp, RT.object_array((Object)Tuple.create((Object)((BTSetSplit)split).left, (Object)((BTSetSplit)split).k, (Object)((BTSetSplit)split).right))));
            } else {
                Keyword keyword = const__8;
                if (keyword != null && keyword != Boolean.FALSE) {
                    new_root = null;
                    bTSet = new BTSet(this.cmp, this.cnt + 1L, new_root);
                } else {
                    bTSet = null;
                }
            }
        }
        return bTSet;
    }

    public IPersistentCollection empty() {
        return new BTSet(this.cmp, 0L, null);
    }
}

