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
import datomic.impl.db.IDatum;
import java.util.Comparator;

public final class db$reify__12541
implements Comparator,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Object const__4 = -1L;
    public static final Object const__6 = 1L;
    public static final Keyword const__7 = RT.keyword(null, (String)"else");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"assertion-policy-compare");

    public db$reify__12541(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public db$reify__12541() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new db$reify__12541(iPersistentMap);
    }

    public int compare(Object x, Object y) {
        Object object;
        Object object2 = x;
        x = null;
        Object x2 = object2;
        Object object3 = y;
        y = null;
        Object y2 = object3;
        long c = ((IFn.OOL)const__0.getRawRoot()).invokePrim(((IDatum)x2).getV(), ((IDatum)y2).getV());
        Object object4 = ((IFn)const__1.getRawRoot()).invoke((Object)(Numbers.isZero((long)c) ? Boolean.TRUE : Boolean.FALSE));
        if (object4 != null && object4 != Boolean.FALSE) {
            object = Numbers.num((long)c);
        } else if ((long)((IDatum)x2).getA() < (long)((IDatum)y2).getA()) {
            object = const__4;
        } else if ((long)((IDatum)x2).getA() > (long)((IDatum)y2).getA()) {
            object = const__6;
        } else if (((IDatum)x2).getE() < ((IDatum)y2).getE()) {
            object = const__4;
        } else if (((IDatum)x2).getE() > ((IDatum)y2).getE()) {
            object = const__6;
        } else if (((IDatum)x2).getT() > ((IDatum)y2).getT()) {
            object = const__4;
        } else if (((IDatum)x2).getT() < ((IDatum)y2).getT()) {
            object = const__6;
        } else {
            Keyword keyword = const__7;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object5 = x2;
                x2 = null;
                Object object6 = y2;
                y2 = null;
                db$reify__12541 this_ = null;
                object = ((IFn)const__8.getRawRoot()).invoke(object5, object6);
            } else {
                object = null;
            }
        }
        return ((Number)object).intValue();
    }
}

