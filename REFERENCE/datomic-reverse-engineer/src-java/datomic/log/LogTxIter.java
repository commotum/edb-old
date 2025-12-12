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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.iter.Iter;
import datomic.log.LogDir;
import datomic.log.LogDirSeq;
import datomic.log.LogSeek;
import datomic.log.LogSegSeq;
import datomic.log.LogTxIter$iter__16252__16256;
import datomic.log.LogTxIter$iter__16265__16271;

public final class LogTxIter
implements LogDirSeq,
Iter,
LogSegSeq,
IType {
    public final Object lookup;
    public final Object root_val;
    public final Object tail;
    long ridx;
    Object dir;
    long didx;
    Object seg;
    long sidx;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Keyword const__14;

    public LogTxIter(Object object, Object object2, Object object3, long l, Object object4, long l2, Object object5, long l3) {
        this.lookup = object;
        this.root_val = object2;
        this.tail = object3;
        this.ridx = l;
        this.dir = object4;
        this.didx = l2;
        this.seg = object5;
        this.sidx = l3;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"lookup"), Symbol.intern(null, (String)"root-val"), Symbol.intern(null, (String)"tail"), ((IObj)Symbol.intern(null, (String)"ridx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"dir")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"didx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"seg")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), ((IObj)Symbol.intern(null, (String)"sidx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE}))});
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object next() {
        Object object;
        block6: {
            boolean and__5236__auto__16289;
            long didx;
            long i;
            Object dir;
            block7: {
                block5: {
                    if (Numbers.inc((long)this_.sidx) < (long)RT.count((Object)this_.seg)) {
                        this_.sidx = Numbers.inc((long)this_.sidx);
                        object = this_;
                        return object;
                    }
                    if (Numbers.inc((long)this_.didx) >= (long)RT.count((Object)this_.dir)) break block5;
                    this_.didx = Numbers.inc((long)this_.didx);
                    if (this_.didx % 2L != 0L) break block6;
                    dir = this_.dir;
                    i = 0L;
                    didx = Numbers.add((long)4L, (long)this_.didx);
                    break block7;
                }
                if (Numbers.inc((long)this_.ridx) < (long)RT.count((Object)this_.root_val)) {
                    this_.ridx = Numbers.inc((long)this_.ridx);
                    this_.dir = ((IFn)const__12.getRawRoot()).invoke(this_.lookup, ((LogDir)RT.nth((Object)this_.root_val, (int)RT.intCast((long)this_.ridx))).uuid);
                    this_.didx = 0L;
                    this_.seg = ((IFn)const__12.getRawRoot()).invoke(this_.lookup, ((LogDir)RT.nth((Object)this_.dir, (int)RT.intCast((long)this_.didx))).uuid);
                    this_.sidx = 0L;
                    object = this_;
                    return object;
                }
                Object object2 = this_.tail;
                if (Util.classOf((Object)object2) != __cached_class__0) {
                    if (object2 instanceof LogSeek) {
                        object = ((LogSeek)object2).seek_tx_impl(Numbers.inc((Object)((IFn)const__12.getRawRoot()).invoke(((Iter)this_).get(), (Object)const__14)));
                        return object;
                    }
                    object2 = object2;
                    __cached_class__0 = Util.classOf((Object)object2);
                }
                Number number = Numbers.inc((Object)((IFn)const__12.getRawRoot()).invoke(((Iter)this_).get(), (Object)const__14));
                LogTxIter this_ = null;
                object = const__13.getRawRoot().invoke(object2, (Object)number);
                return object;
            }
            while ((and__5236__auto__16289 = Numbers.lt((long)i, (long)2L)) ? Numbers.lt((long)didx, (long)RT.count((Object)dir)) : and__5236__auto__16289) {
                Object k;
                Object object3 = k = ((LogDir)RT.nth((Object)dir, (int)RT.intCast((long)didx))).uuid;
                k = null;
                ((IFn)const__11.getRawRoot()).invoke(this_.lookup, object3);
                Object object4 = dir;
                dir = null;
                didx = Numbers.inc((long)didx);
                i = Numbers.inc((long)i);
                dir = object4;
            }
        }
        this_.seg = ((IFn)const__12.getRawRoot()).invoke(this_.lookup, ((LogDir)RT.nth((Object)this_.dir, (int)RT.intCast((long)this_.didx))).uuid);
        this_.sidx = 0L;
        object = this_;
        return object;
    }

    public Object get() {
        LogTxIter this_ = null;
        return RT.nth((Object)this_.seg, (int)RT.intCast((long)this_.sidx));
    }

    public Object log_seg_seq() {
        LogTxIter$iter__16265__16271 iter__6025__auto__16290;
        LogTxIter$iter__16265__16271 logTxIter$iter__16265__16271 = iter__6025__auto__16290 = new LogTxIter$iter__16265__16271(this_.didx, this_.root_val, this_.lookup);
        iter__6025__auto__16290 = null;
        LogTxIter this_ = null;
        return ((IFn)logTxIter$iter__16265__16271).invoke(((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)this_.ridx), (Object)RT.count((Object)this_.root_val)));
    }

    public Object log_dir_seq() {
        LogTxIter$iter__16252__16256 iter__6025__auto__16291;
        LogTxIter$iter__16252__16256 logTxIter$iter__16252__16256 = iter__6025__auto__16291 = new LogTxIter$iter__16252__16256(this_.root_val, this_.lookup);
        iter__6025__auto__16291 = null;
        LogTxIter this_ = null;
        return ((IFn)logTxIter$iter__16252__16256).invoke(((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)this_.ridx), (Object)RT.count((Object)this_.root_val)));
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"range");
        const__11 = RT.var((String)"datomic.cache", (String)"read-ahead");
        const__12 = RT.var((String)"datomic.common", (String)"getx");
        const__13 = RT.var((String)"datomic.log", (String)"seek-tx-impl");
        const__14 = RT.keyword(null, (String)"t");
    }
}

