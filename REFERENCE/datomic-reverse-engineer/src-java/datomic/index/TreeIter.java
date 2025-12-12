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
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.index.DirNode;
import datomic.index.ITreeIter;
import datomic.index.RootNode;
import datomic.index.TransposedData;
import datomic.index.TreeIter$fn__15117;
import datomic.index.TreeIter$iter__15092__15098;
import datomic.index.TreeIter$iter__15121__15127;
import datomic.index.TreeIter$iter__15146__15152;
import datomic.iter.Iter;

public final class TreeIter
implements ITreeIter,
Iter,
IType {
    public final Object lookup;
    public final Object root;
    int ridx;
    Object dir;
    int didx;
    Object seg;
    int sidx;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__14 = RT.var((String)"datomic.cache", (String)"read-ahead");
    public static final Var const__15 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__16 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Keyword const__17 = RT.keyword(null, (String)"else");

    public TreeIter(Object object, Object object2, int n, Object object3, int n2, Object object4, int n3) {
        this.lookup = object;
        this.root = object2;
        this.ridx = n;
        this.dir = object3;
        this.didx = n2;
        this.seg = object4;
        this.sidx = n3;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"lookup"), ((IObj)Symbol.intern(null, (String)"root")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"RootNode")})), ((IObj)Symbol.intern(null, (String)"ridx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"int"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"dir")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"DirNode"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"didx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"int"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"seg")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"TransposedData"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"sidx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"int"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE}))});
    }

    public Object prev() {
        TreeIter treeIter;
        if ((long)this.sidx - 1L >= (long)Numbers.ints((Object)((DirNode)this.dir).offsets)[this.didx]) {
            this.sidx = (int)((long)this.sidx - 1L);
            treeIter = this;
        } else if ((long)this.didx - 1L >= 0L) {
            this.didx = (int)((long)this.didx - 1L);
            this.seg = ((IFn)const__15.getRawRoot()).invoke(this.lookup, RT.aget((Object[])((Object[])((DirNode)this.dir).segids), (int)this.didx));
            this.sidx = (int)((long)Numbers.ints((Object)((DirNode)this.dir).offsets)[this.didx] + (long)Numbers.ints((Object)((DirNode)this.dir).counts)[this.didx] + -1L);
            treeIter = this;
        } else if ((long)this.ridx - 1L >= 0L) {
            this.ridx = (int)((long)this.ridx - 1L);
            this.dir = ((IFn)const__16.getRawRoot()).invoke(this.root, (Object)this.ridx, this.lookup, (Object)Boolean.FALSE);
            this.didx = (int)((long)RT.count((Object)((DirNode)this.dir).segids) - 1L);
            this.seg = ((IFn)const__15.getRawRoot()).invoke(this.lookup, RT.aget((Object[])((Object[])((DirNode)this.dir).segids), (int)this.didx));
            this.sidx = (int)((long)Numbers.ints((Object)((DirNode)this.dir).offsets)[this.didx] + (long)Numbers.ints((Object)((DirNode)this.dir).counts)[this.didx] + -1L);
            treeIter = this;
        } else {
            Keyword keyword = const__17;
            treeIter = keyword != null && keyword != Boolean.FALSE ? null : null;
        }
        return treeIter;
    }

    public Object next() {
        TreeIter treeIter;
        if ((long)this.sidx + 1L < (long)Numbers.ints((Object)((DirNode)this.dir).offsets)[this.didx] + (long)Numbers.ints((Object)((DirNode)this.dir).counts)[this.didx]) {
            this.sidx = (int)((long)this.sidx + 1L);
            treeIter = this;
        } else if ((long)this.didx + 1L < (long)RT.count((Object)((DirNode)this.dir).segids)) {
            this.didx = (int)((long)this.didx + 1L);
            if ((long)this.didx % 2L == 0L) {
                boolean and__5236__auto__15172;
                Object dir = this.dir;
                long i = 0L;
                long didx = 4L + (long)this.didx;
                while ((and__5236__auto__15172 = Numbers.lt((long)i, (long)2L)) ? Numbers.lt((long)didx, (long)RT.count((Object)((DirNode)dir).segids)) : and__5236__auto__15172) {
                    Object k;
                    Object object = k = RT.aget((Object[])((Object[])((DirNode)dir).segids), (int)((int)didx));
                    k = null;
                    ((IFn)const__14.getRawRoot()).invoke(this.lookup, object);
                    Object object2 = dir;
                    dir = null;
                    ++didx;
                    ++i;
                    dir = object2;
                }
            }
            this.seg = ((IFn)const__15.getRawRoot()).invoke(this.lookup, RT.aget((Object[])((Object[])((DirNode)this.dir).segids), (int)this.didx));
            this.sidx = Numbers.ints((Object)((DirNode)this.dir).offsets)[this.didx];
            treeIter = this;
        } else if ((long)this.ridx + 1L < (long)RT.count((Object)((RootNode)this.root).dirids)) {
            this.ridx = (int)((long)this.ridx + 1L);
            this.dir = ((IFn)const__16.getRawRoot()).invoke(this.root, (Object)this.ridx, this.lookup, (Object)Boolean.FALSE);
            this.didx = (int)0L;
            this.seg = ((IFn)const__15.getRawRoot()).invoke(this.lookup, RT.aget((Object[])((Object[])((DirNode)this.dir).segids), (int)this.didx));
            this.sidx = Numbers.ints((Object)((DirNode)this.dir).offsets)[this.didx];
            treeIter = this;
        } else {
            Keyword keyword = const__17;
            treeIter = keyword != null && keyword != Boolean.FALSE ? null : null;
        }
        return treeIter;
    }

    public Object get() {
        return ((TransposedData)this.seg).get(this.sidx);
    }

    public Object dir_seq() {
        TreeIter$iter__15146__15152 iter__6025__auto__15173;
        TreeIter$iter__15146__15152 treeIter$iter__15146__15152 = iter__6025__auto__15173 = new TreeIter$iter__15146__15152(this_.lookup, this_.root, this_.ridx, this_.didx);
        iter__6025__auto__15173 = null;
        TreeIter this_ = null;
        return ((IFn)treeIter$iter__15146__15152).invoke(((IFn)const__0.getRawRoot()).invoke((Object)this_.ridx, (Object)RT.count((Object)((RootNode)this_.root).dirids)));
    }

    public Object seg_seq() {
        TreeIter$iter__15121__15127 iter__6025__auto__15174;
        TreeIter$iter__15121__15127 treeIter$iter__15121__15127 = iter__6025__auto__15174 = new TreeIter$iter__15121__15127(this_.lookup, this_.root, this_.ridx, this_.didx);
        iter__6025__auto__15174 = null;
        TreeIter this_ = null;
        return ((IFn)treeIter$iter__15121__15127).invoke(((IFn)const__0.getRawRoot()).invoke((Object)this_.ridx, (Object)RT.count((Object)((RootNode)this_.root).dirids)));
    }

    public Object seg_PLUS_item_seq() {
        Object segids;
        TreeIter$iter__15092__15098 iter__6025__auto__15175;
        TreeIter$iter__15092__15098 treeIter$iter__15092__15098 = iter__6025__auto__15175 = new TreeIter$iter__15092__15098(this_.lookup, this_.root, this_.ridx, this_.didx);
        iter__6025__auto__15175 = null;
        Object object = segids = ((IFn)treeIter$iter__15092__15098).invoke(((IFn)const__0.getRawRoot()).invoke((Object)this_.ridx, (Object)RT.count((Object)((RootNode)this_.root).dirids)));
        segids = null;
        TreeIter this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)new TreeIter$fn__15117(this_.lookup), object);
    }
}

