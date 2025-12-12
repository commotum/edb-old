/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Datum;
import datomic.index.IBinarySearch;
import datomic.index.IndexedComparator;
import datomic.index.TransposedData;

public final class index$reify__15189
implements IndexedComparator,
IBinarySearch,
IObj {
    final IPersistentMap __meta;
    public static final Object const__1 = 0L;
    public static final Var const__11 = RT.var((String)"datomic.common", (String)"compare");
    public static final Object const__13 = 1L;
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"not");
    public static final Object const__18 = -1L;
    public static final Keyword const__20 = RT.keyword(null, (String)"else");

    public index$reify__15189(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public index$reify__15189() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$reify__15189(iPersistentMap);
    }

    public long compare(Datum x, TransposedData ys, long i) {
        Object object;
        long c = ((IFn.OOL)const__11.getRawRoot()).invokePrim(x.getV(), ys.getV(RT.uncheckedIntCast((long)i)));
        long eai = i << (int)1L;
        long a = ((long[])ys.eas)[(int)(eai + 1L)];
        long e = ((long[])ys.eas)[(int)eai];
        Object object2 = ((IFn)const__16.getRawRoot()).invoke((Object)(Numbers.isZero((long)c) ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = Numbers.num((long)c);
        } else if ((long)x.getA() < a) {
            object = const__18;
        } else if ((long)x.getA() > a) {
            object = const__13;
        } else if (x.getE() < e) {
            object = const__18;
        } else if (x.getE() > e) {
            object = const__13;
        } else if (x.getT() > ys.getT(RT.uncheckedIntCast((long)i))) {
            object = const__18;
        } else if (x.getT() < ys.getT(RT.uncheckedIntCast((long)i))) {
            object = const__13;
        } else {
            TransposedData transposedData = ys;
            ys = null;
            if (x.isAssertion() == transposedData.isAssertion(RT.uncheckedIntCast((long)i))) {
                object = const__1;
            } else {
                Datum datum2 = x;
                x = null;
                if (datum2.isAssertion()) {
                    object = const__18;
                } else {
                    Keyword keyword = const__20;
                    object = keyword != null && keyword != Boolean.FALSE ? const__13 : null;
                }
            }
        }
        return ((Number)object).longValue();
    }

    public long search(TransposedData tdata, Object k) {
        long l;
        block3: {
            long low = 0L;
            long high = (long)tdata.size() - 1L;
            while (low <= high) {
                long mid = (low + high) / 2L;
                long c = ((IndexedComparator)this).compare((Datum)k, tdata, mid);
                if (c > 0L) {
                    low = mid + 1L;
                    continue;
                }
                if (c < 0L) {
                    high = mid - 1L;
                    continue;
                }
                l = mid;
                break block3;
            }
            l = -(low + 1L);
        }
        return l;
    }
}

