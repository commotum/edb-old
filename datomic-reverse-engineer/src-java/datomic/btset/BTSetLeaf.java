/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.btset;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.BTSetIter;
import datomic.btset.BTSetSplit;
import datomic.btset.IBTSetIterLink;
import datomic.btset.IBTSetLeaf;
import datomic.btset.IBTSetNode;
import datomic.iter.Iter;
import java.util.Comparator;

public final class BTSetLeaf
implements IBTSetLeaf,
IBTSetNode,
IType {
    public final long cnt;
    public final Object cmp;
    public final Object ks;
    public static final Object const__3 = 16L;
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"=");
    public static final Keyword const__15 = RT.keyword(null, (String)"else");

    public BTSetLeaf(long l, Object object, Object object2) {
        this.cnt = l;
        this.cmp = object;
        this.ks = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cnt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.Comparator")})), (Object)((IObj)Symbol.intern(null, (String)"ks")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})));
    }

    public Iter rseek(IBTSetIterLink path2) {
        IBTSetIterLink iBTSetIterLink = path2;
        path2 = null;
        return new BTSetIter(iBTSetIterLink, this, this.cnt - 1L);
    }

    public Iter seek(Object k, IBTSetIterLink path2) {
        Object object;
        block4: {
            long split = this.cnt / 2L;
            long i = ((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.ks), (int)((int)split))) < 0L ? 0L : split;
            while (true) {
                boolean or__5238__auto__11839;
                if ((or__5238__auto__11839 = Util.equiv((long)i, (long)this.cnt)) ? or__5238__auto__11839 : Numbers.lte((long)((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.ks), (int)((int)i))), (long)0L)) {
                    BTSetIter ret = new BTSetIter(path2, this, i);
                    if (i == this.cnt) {
                        BTSetIter bTSetIter = ret;
                        ret = null;
                        object = bTSetIter.next();
                    } else {
                        object = ret;
                        ret = null;
                    }
                    break block4;
                }
                Keyword keyword = const__15;
                if (keyword == null || keyword == Boolean.FALSE) break;
                ++i;
            }
            object = null;
        }
        return (Iter)object;
    }

    public Iter seek(IBTSetIterLink path2) {
        IBTSetIterLink iBTSetIterLink = path2;
        path2 = null;
        return new BTSetIter(iBTSetIterLink, this, 0L);
    }

    public Object conjoin(Object k) {
        Object object;
        block7: {
            long half_leaf = 16L / 2L;
            long split = this.cnt / 2L;
            long i = ((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.ks), (int)((int)split))) < 0L ? 0L : split;
            while (true) {
                boolean or__5238__auto__11840;
                if ((or__5238__auto__11840 = Util.equiv((long)i, (long)this.cnt)) ? or__5238__auto__11840 : Numbers.isNeg((long)((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.ks), (int)((int)i))))) {
                    Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)Numbers.num((long)i), (Object)Numbers.num((long)this.cnt), const__3);
                    if (object2 != null && object2 != Boolean.FALSE) {
                        object = new BTSetSplit(this, k, new BTSetLeaf(1L, this.cmp, RT.object_array((Object)Tuple.create((Object)k))));
                    } else {
                        Object[] new_ks = RT.object_array((Object)Numbers.num((long)Numbers.unchecked_inc((long)this.cnt)));
                        System.arraycopy(this.ks, RT.uncheckedIntCast((long)0L), new_ks, RT.uncheckedIntCast((long)0L), RT.uncheckedIntCast((long)i));
                        RT.aset((Object[])new_ks, (int)((int)i), (Object)k);
                        System.arraycopy(this.ks, RT.uncheckedIntCast((long)i), new_ks, RT.uncheckedIntCast((long)(i + 1L)), RT.uncheckedIntCast((long)(this.cnt - i)));
                        if (this.cnt < 16L) {
                            new_ks = null;
                            object = new BTSetLeaf(this.cnt + 1L, this.cmp, new_ks);
                        } else {
                            Object[] aks = RT.object_array((Object)Numbers.num((long)Numbers.unchecked_inc((long)half_leaf)));
                            Object[] bks = RT.object_array((Object)Numbers.num((long)half_leaf));
                            System.arraycopy(new_ks, RT.uncheckedIntCast((long)0L), aks, RT.uncheckedIntCast((long)0L), RT.uncheckedIntCast((long)(half_leaf + 1L)));
                            Object[] objectArray = new_ks;
                            new_ks = null;
                            System.arraycopy(objectArray, RT.uncheckedIntCast((long)(half_leaf + 1L)), bks, RT.uncheckedIntCast((long)0L), RT.uncheckedIntCast((long)half_leaf));
                            aks = null;
                            bks = null;
                            object = new BTSetSplit(new BTSetLeaf(half_leaf + 1L, this.cmp, aks), RT.aget((Object[])bks, (int)((int)0L)), new BTSetLeaf(half_leaf, this.cmp, bks));
                        }
                    }
                    break block7;
                }
                if (((IBTSetNode)this).compare(RT.aget((Object[])((Object[])this.ks), (int)((int)i)), k) == 0L) {
                    object = this;
                    break block7;
                }
                Keyword keyword = const__15;
                if (keyword == null || keyword == Boolean.FALSE) break;
                ++i;
            }
            object = null;
        }
        return object;
    }

    public long compare(Object x, Object y) {
        int n;
        BTSetLeaf this_;
        Object object = this_.cmp;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = x;
            x = null;
            Object object3 = y;
            y = null;
            this_ = null;
            n = ((Comparator)this_.cmp).compare(object2, object3);
        } else {
            Object object4 = x;
            x = null;
            Object object5 = y;
            y = null;
            this_ = null;
            n = ((Comparable)object4).compareTo(object5);
        }
        return n;
    }

    public Object keyAt(long n) {
        BTSetLeaf this_ = null;
        return RT.aget((Object[])((Object[])this_.ks), (int)((int)n));
    }

    public long count() {
        return this.cnt;
    }
}

