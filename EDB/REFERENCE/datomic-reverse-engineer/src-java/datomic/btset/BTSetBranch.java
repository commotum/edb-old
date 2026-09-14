/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 */
package datomic.btset;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import datomic.btset.BTSetIterLink;
import datomic.btset.BTSetSplit;
import datomic.btset.IBTSetBranch;
import datomic.btset.IBTSetIterLink;
import datomic.btset.IBTSetNode;
import datomic.iter.Iter;
import java.util.Comparator;

public final class BTSetBranch
implements IBTSetNode,
IBTSetBranch,
IType {
    public final Object cmp;
    public final Object nks;
    public static final Keyword const__20 = RT.keyword(null, (String)"else");

    public BTSetBranch(Object object, Object object2) {
        this.cmp = object;
        this.nks = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.Comparator")})), (Object)((IObj)Symbol.intern(null, (String)"nks")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})));
    }

    public Iter rseek(IBTSetIterLink path2) {
        IBTSetIterLink iBTSetIterLink = path2;
        path2 = null;
        return ((IBTSetNode)RT.aget((Object[])((Object[])this.nks), (int)((int)((long)((Object[])this.nks).length - 1L)))).rseek(new BTSetIterLink(this, ((IBTSetBranch)this).count() - 1L, iBTSetIterLink));
    }

    public Iter seek(Object k, IBTSetIterLink path2) {
        boolean or__5238__auto__11832;
        long split = 2L * ((long)((Object[])this.nks).length / 4L);
        long pos = ((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.nks), (int)((int)(split + 1L)))) < 0L ? 0L : split;
        while (!((or__5238__auto__11832 = Util.equiv((long)pos, (long)((long)((Object[])this.nks).length - 1L))) ? or__5238__auto__11832 : Numbers.isNeg((long)((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.nks), (int)((int)(pos + 1L))))))) {
            pos += 2L;
        }
        return ((IBTSetNode)RT.aget((Object[])((Object[])this.nks), (int)((int)pos))).seek(k, new BTSetIterLink(this, pos / 2L, path2));
    }

    public Iter seek(IBTSetIterLink path2) {
        IBTSetIterLink iBTSetIterLink = path2;
        path2 = null;
        return ((IBTSetNode)RT.aget((Object[])((Object[])this.nks), (int)((int)0L))).seek(new BTSetIterLink(this, 0L, iBTSetIterLink));
    }

    public Object conjoin(Object k) {
        boolean or__5238__auto__11833;
        long split = 2L * ((long)((Object[])this.nks).length / 4L);
        long pos = ((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.nks), (int)((int)(split + 1L)))) < 0L ? 0L : split;
        while (!((or__5238__auto__11833 = Util.equiv((long)pos, (long)((long)((Object[])this.nks).length - 1L))) ? or__5238__auto__11833 : Numbers.isNeg((long)((IBTSetNode)this).compare(k, RT.aget((Object[])((Object[])this.nks), (int)((int)(pos + 1L))))))) {
            pos = 2L + pos;
        }
        return ((IBTSetBranch)this).upsert(pos, ((IBTSetNode)RT.aget((Object[])((Object[])this.nks), (int)((int)pos))).conjoin(k));
    }

    public long compare(Object x, Object y) {
        int n;
        BTSetBranch this_;
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

    public Object upsert(long pos, Object object) {
        Object object2;
        if (Util.identical((Object)object, (Object)RT.aget((Object[])((Object[])this.nks), (int)((int)pos)))) {
            object2 = this;
        } else if (object instanceof BTSetSplit) {
            long half_branch = 16L / 2L;
            Object object3 = object;
            object = null;
            Object c = object3;
            Object[] new_nks = RT.object_array((Object)Numbers.num((long)Numbers.unchecked_add((long)2L, (long)((Object[])this.nks).length)));
            System.arraycopy(this.nks, RT.uncheckedIntCast((long)0L), new_nks, RT.uncheckedIntCast((long)0L), RT.uncheckedIntCast((long)pos));
            RT.aset((Object[])new_nks, (int)((int)pos), (Object)((BTSetSplit)c).left);
            RT.aset((Object[])new_nks, (int)((int)(1L + pos)), (Object)((BTSetSplit)c).k);
            Object object4 = c;
            c = null;
            RT.aset((Object[])new_nks, (int)((int)(2L + pos)), (Object)((BTSetSplit)object4).right);
            System.arraycopy(this.nks, RT.uncheckedIntCast((long)(pos + 1L)), new_nks, RT.uncheckedIntCast((long)(3L + pos)), RT.uncheckedIntCast((long)((long)((Object[])this.nks).length - pos - 1L)));
            if ((long)new_nks.length <= 16L) {
                new_nks = null;
                object2 = new BTSetBranch(this.cmp, new_nks);
            } else {
                Object[] anks = RT.object_array((Object)Numbers.num((long)Numbers.unchecked_inc((long)half_branch)));
                Object[] bnks = RT.object_array((Object)Numbers.num((long)Numbers.unchecked_dec((long)half_branch)));
                System.arraycopy(new_nks, RT.uncheckedIntCast((long)0L), anks, RT.uncheckedIntCast((long)0L), anks.length);
                System.arraycopy(new_nks, RT.uncheckedIntCast((long)(2L + 16L / 2L)), bnks, RT.uncheckedIntCast((long)0L), bnks.length);
                anks = null;
                new_nks = null;
                bnks = null;
                object2 = new BTSetSplit(new BTSetBranch(this.cmp, anks), RT.aget((Object[])new_nks, (int)((int)(half_branch + 1L))), new BTSetBranch(this.cmp, bnks));
            }
        } else {
            Keyword keyword = const__20;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object[] new_nks = RT.aclone((Object[])((Object[])this.nks));
                Object object5 = object;
                object = null;
                RT.aset((Object[])new_nks, (int)((int)pos), (Object)object5);
                new_nks = null;
                object2 = new BTSetBranch(this.cmp, new_nks);
            } else {
                object2 = null;
            }
        }
        return object2;
    }

    public Object childAt(long i) {
        BTSetBranch this_ = null;
        return RT.aget((Object[])((Object[])this_.nks), (int)((int)(2L * i)));
    }

    public long count() {
        return (long)((Object[])this.nks).length / 2L + 1L;
    }
}

