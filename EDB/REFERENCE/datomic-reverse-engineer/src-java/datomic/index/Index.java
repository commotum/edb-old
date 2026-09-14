/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentSet
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Seqable
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.Counted;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Seqable;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset.IDataSet;
import datomic.index.DirNode;
import datomic.index.IIndex;
import datomic.index.Index$iter__15197__15203;
import datomic.index.RootNode;
import datomic.index.TreeIter;
import datomic.iter.Iter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public final class Index
implements IIndex,
IPersistentSet,
Counted,
IDataSet,
Seqable,
IType {
    public final Object lookup;
    public final Object cmpi;
    public final Object root;
    public final Object cached_count;
    public final Object order;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__7 = RT.var((String)"datomic.index", (String)"ibtree-search");
    public static final Var const__8 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__16 = RT.keyword(null, (String)"count");
    public static final Var const__17 = RT.var((String)"datomic.measure.io-stats", (String)"using-index!");
    public static final Var const__20 = RT.var((String)"datomic.common", (String)"getx");
    public static final Object const__22 = 0L;
    public static final Var const__23 = RT.var((String)"datomic.measure.io-trace", (String)"note!");
    public static final Var const__24 = RT.var((String)"datomic.index", (String)"use-array-cache?");
    public static final Var const__25 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Var const__26 = RT.var((String)"datomic.measure.io-stats", (String)"*io-index*");
    public static final Var const__28 = RT.var((String)"datomic.index", (String)"ibinary-search");

    public Index(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.lookup = object;
        this.cmpi = object2;
        this.root = object3;
        this.cached_count = object4;
        this.order = object5;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"cmpi"), (Object)((IObj)Symbol.intern(null, (String)"root")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"RootNode")})), (Object)Symbol.intern(null, (String)"cached-count"), (Object)Symbol.intern(null, (String)"order"));
    }

    public Iter seek(Object k) {
        Object object;
        ((IFn)const__17.getRawRoot()).invoke(this.order);
        if ((long)((Object[])((RootNode)this.root).dirids).length > 0L) {
            long ridx = ((IFn.OOOL)const__7.getRawRoot()).invokePrim(((RootNode)this.root).keydata, k, this.cmpi);
            Object dir = ((IFn)const__8.getRawRoot()).invoke(this.root, (Object)Numbers.num((long)ridx), this.lookup, (Object)Boolean.TRUE);
            if ((long)((Object[])((DirNode)dir).segids).length > 0L) {
                Object sidx;
                Object object2;
                Object or__5238__auto__15223;
                Object object3;
                Object seg;
                Object ac;
                Object segk;
                long didx = ((IFn.OOOL)const__7.getRawRoot()).invokePrim(((DirNode)dir).keydata, k, this.cmpi);
                Object object4 = segk = RT.aget((Object[])((Object[])((DirNode)dir).segids), (int)((int)didx));
                segk = null;
                ((IFn)const__23.getRawRoot()).invoke(object4, this.order);
                Object object5 = ac = ((IFn)const__24.getRawRoot()).invoke();
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object temp__5457__auto__15222;
                    Object object6;
                    Object and__5236__auto__15221;
                    Object object7 = and__5236__auto__15221 = (seg = RT.aget((Object[])((Object[])((DirNode)dir).segs), (int)((int)didx)));
                    if (object7 != null && object7 != Boolean.FALSE) {
                        Object object8 = seg;
                        seg = null;
                        object6 = ((Reference)object8).get();
                    } else {
                        object6 = and__5236__auto__15221;
                        and__5236__auto__15221 = null;
                    }
                    Object object9 = temp__5457__auto__15222 = object6;
                    if (object9 != null && object9 != Boolean.FALSE) {
                        Object object10 = temp__5457__auto__15222;
                        temp__5457__auto__15222 = null;
                        Object ret = object10;
                        ((IFn)const__25.getRawRoot()).invoke(const__26.get());
                        object3 = ret;
                        ret = null;
                    } else {
                        object3 = null;
                    }
                } else {
                    object3 = null;
                }
                Object object11 = or__5238__auto__15223 = object3;
                if (object11 != null && object11 != Boolean.FALSE) {
                    object2 = or__5238__auto__15223;
                    or__5238__auto__15223 = null;
                } else {
                    Object seg2 = ((IFn)const__20.getRawRoot()).invoke(this.lookup, RT.aget((Object[])((Object[])((DirNode)dir).segids), (int)((int)didx)));
                    Object object12 = ac;
                    ac = null;
                    if (object12 != null && object12 != Boolean.FALSE) {
                        RT.aset((Object[])((Object[])((DirNode)dir).segs), (int)((int)didx), new WeakReference<Object>(seg2));
                    }
                    object2 = seg2;
                    seg2 = null;
                }
                seg = object2;
                int doff = ((int[])((DirNode)dir).offsets)[(int)didx];
                int dcount = ((int[])((DirNode)dir).counts)[(int)didx];
                Object object13 = k;
                k = null;
                Object object14 = sidx = ((IFn)const__28.getRawRoot()).invoke(seg, object13, this.cmpi);
                if (object14 != null && object14 != Boolean.FALSE) {
                    dir = null;
                    seg = null;
                    sidx = null;
                    object = new TreeIter(this.lookup, this.root, RT.uncheckedIntCast((long)ridx), dir, RT.uncheckedIntCast((long)didx), seg, RT.uncheckedIntCast((long)(RT.longCast((Object)sidx) + (long)doff)));
                } else {
                    Object object15 = dir;
                    dir = null;
                    Object object16 = seg;
                    seg = null;
                    object = new TreeIter(this.lookup, this.root, RT.uncheckedIntCast((long)ridx), object15, RT.uncheckedIntCast((long)didx), object16, RT.uncheckedIntCast((long)((long)doff + (long)dcount + -1L))).next();
                }
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return (Iter)object;
    }

    public Iter seek() {
        TreeIter treeIter;
        ((IFn)const__17.getRawRoot()).invoke(this.order);
        if ((long)((Object[])((RootNode)this.root).dirids).length > 0L) {
            Object dir = ((IFn)const__8.getRawRoot()).invoke(this.root, const__22, this.lookup, (Object)Boolean.FALSE);
            if ((long)RT.count((Object)((DirNode)dir).segids) > 0L) {
                dir = null;
                treeIter = new TreeIter(this.lookup, this.root, RT.uncheckedIntCast((long)0L), dir, RT.uncheckedIntCast((long)0L), ((IFn)const__20.getRawRoot()).invoke(this.lookup, RT.nth((Object)((DirNode)dir).segids, (int)RT.uncheckedIntCast((long)0L))), RT.uncheckedIntCast((Object)((Number)RT.nth((Object)((DirNode)dir).offsets, (int)RT.uncheckedIntCast((long)0L)))));
            } else {
                treeIter = null;
            }
        } else {
            treeIter = null;
        }
        return treeIter;
    }

    public Iter seekLast() {
        TreeIter treeIter;
        Object dirids;
        ((IFn)const__17.getRawRoot()).invoke(this.order);
        Object object = dirids = ((RootNode)this.root).dirids;
        dirids = null;
        long ridx = (long)RT.count((Object)object) - 1L;
        if (ridx < 0L) {
            treeIter = null;
        } else {
            Object dir = ((IFn)const__8.getRawRoot()).invoke(this.root, (Object)Numbers.num((long)ridx), this.lookup, (Object)Boolean.FALSE);
            Object segids = ((DirNode)dir).segids;
            long didx = (long)RT.count((Object)segids) - 1L;
            if (didx < 0L) {
                treeIter = null;
            } else {
                Object object2 = segids;
                segids = null;
                Object seg = ((IFn)const__20.getRawRoot()).invoke(this.lookup, RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)didx)));
                Number segidx = Numbers.unchecked_dec((Object)Numbers.unchecked_add((Object)RT.nth((Object)((DirNode)dir).offsets, (int)RT.uncheckedIntCast((long)didx)), (long)RT.count((Object)seg)));
                dir = null;
                seg = null;
                segidx = null;
                treeIter = new TreeIter(this.lookup, this.root, RT.uncheckedIntCast((long)ridx), dir, RT.uncheckedIntCast((long)didx), seg, RT.uncheckedIntCast((Object)segidx));
            }
        }
        return treeIter;
    }

    public long longCount() {
        Object object;
        Object or__5238__auto__15224;
        Object object2 = or__5238__auto__15224 = ((IFn)const__11.getRawRoot()).invoke(this_.cached_count);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__15224;
            or__5238__auto__15224 = null;
        } else {
            Object object3 = ((IFn)const__13.getRawRoot()).invoke(const__14.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke((Object)const__16, (Object)this_));
            Index this_ = null;
            object = ((IFn)const__12.getRawRoot()).invoke(this_.cached_count, object3);
        }
        return ((Number)object).longValue();
    }

    public Object seek_seg(Object k) {
        Object object;
        if ((long)((Object[])((RootNode)this_.root).dirids).length > 0L) {
            long ridx = ((IFn.OOOL)const__7.getRawRoot()).invokePrim(((RootNode)this_.root).keydata, k, this_.cmpi);
            Object dir = ((IFn)const__8.getRawRoot()).invoke(this_.root, (Object)Numbers.num((long)ridx), this_.lookup, (Object)Boolean.TRUE);
            if ((long)((Object[])((DirNode)dir).segids).length > 0L) {
                Object object2 = k;
                k = null;
                long didx = ((IFn.OOOL)const__7.getRawRoot()).invokePrim(((DirNode)dir).keydata, object2, this_.cmpi);
                Object object3 = dir;
                dir = null;
                Index this_ = null;
                object = RT.aget((Object[])((Object[])((DirNode)object3).segids), (int)((int)didx));
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public ISeq seq() {
        Index$iter__15197__15203 iter__6025__auto__15225;
        Index$iter__15197__15203 index$iter__15197__15203 = iter__6025__auto__15225 = new Index$iter__15197__15203(this_.root, this_.lookup);
        iter__6025__auto__15225 = null;
        Index this_ = null;
        return (ISeq)((IFn)const__2.getRawRoot()).invoke(((IFn)index$iter__15197__15203).invoke(((IFn)const__3.getRawRoot()).invoke((Object)RT.count((Object)((RootNode)this_.root).dirids))));
    }

    public int count() {
        IDataSet iDataSet = this_;
        Index this_ = null;
        return RT.intCast((long)iDataSet.longCount());
    }

    public Object get(Object k) {
        Object object;
        Iter temp__5457__auto__15226;
        Iter iter2 = temp__5457__auto__15226 = ((IDataSet)this).seek(k);
        if (iter2 != null && iter2 != Boolean.FALSE) {
            Iter iter3 = temp__5457__auto__15226;
            temp__5457__auto__15226 = null;
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
        Iter temp__5457__auto__15227;
        Iter iter2 = temp__5457__auto__15227 = ((IDataSet)this).seek(k);
        if (iter2 != null && iter2 != Boolean.FALSE) {
            Iter iter3 = temp__5457__auto__15227;
            temp__5457__auto__15227 = null;
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
}

