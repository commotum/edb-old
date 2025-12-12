/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDatumImpl;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class db$prefetch_redundancy_PLUS_uniqueness$fn__13978$fn__13979
extends AFunction {
    Object redundancy;
    Object db;
    Object uniqueness;
    Object basis;
    Object d;
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"find-aevt");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"long-add!");
    public static final Var const__8 = RT.var((String)"datomic.common", (String)"equals-with-strict-scale");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"find-avet");

    public db$prefetch_redundancy_PLUS_uniqueness$fn__13978$fn__13979(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.redundancy = object;
        this.db = object2;
        this.uniqueness = object3;
        this.basis = object4;
        this.d = object5;
    }

    public Object invoke() {
        Object object;
        Object object2;
        Object and__5236__auto__13987;
        Boolean redundant_QMARK_;
        Object object3;
        Object object4;
        Boolean ed;
        Boolean and__5236__auto__13985;
        Object object5;
        boolean and__5236__auto__13984;
        long eidx = ((IDatumImpl)this.d).eidx();
        int attrid = ((IDatum)this.d).getA();
        Object v = ((IDatum)this.d).getV();
        Object attr = ((IDbImpl)this.db).elementAt(attrid);
        boolean card_one_QMARK_ = Util.equiv((Object)((Attribute)attr).cardinality, (long)35L);
        boolean or__5238__auto__13981 = Numbers.isZero((Object)this.basis);
        boolean bl = and__5236__auto__13984 = or__5238__auto__13981 ? or__5238__auto__13981 : Numbers.lt((long)eidx, (Object)this.basis);
        if (and__5236__auto__13984) {
            long start__13414__auto__13982 = System.nanoTime();
            Object ret__13415__auto__13983 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(this.db, (Object)((IDatum)this.d).getA(), (Object)Numbers.num((long)((IDatum)this.d).getE()), card_one_QMARK_ ? null : ((IDatum)this.d).getV()));
            ((IFn.OLO)const__6.getRawRoot()).invokePrim(this.redundancy, System.nanoTime() - start__13414__auto__13982);
            object5 = ret__13415__auto__13983;
            ret__13415__auto__13983 = null;
        } else {
            object5 = and__5236__auto__13984 ? Boolean.TRUE : Boolean.FALSE;
        }
        Boolean bl2 = and__5236__auto__13985 = (ed = object5);
        if (bl2 != null && bl2 != Boolean.FALSE) {
            Boolean bl3 = ed;
            ed = null;
            object4 = ((IFn)const__8.getRawRoot()).invoke(v, ((IDatum)((Object)bl3)).getV());
        } else {
            object4 = and__5236__auto__13985;
            and__5236__auto__13985 = null;
        }
        Boolean there_QMARK_ = object4;
        if (((IDatum)this.d).isAssertion()) {
            object3 = there_QMARK_;
            there_QMARK_ = null;
        } else {
            Boolean bl4 = there_QMARK_;
            there_QMARK_ = null;
            object3 = ((IFn)const__9.getRawRoot()).invoke((Object)bl4);
        }
        Boolean bl5 = redundant_QMARK_ = object3;
        redundant_QMARK_ = null;
        Object object6 = and__5236__auto__13987 = ((IFn)const__9.getRawRoot()).invoke((Object)bl5);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object and__5236__auto__13986;
            Object object7 = attr;
            attr = null;
            Object object8 = and__5236__auto__13986 = ((Attribute)object7).unique;
            if (object8 != null && object8 != Boolean.FALSE) {
                object2 = ((IDatum)this.d).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13986;
                and__5236__auto__13986 = null;
            }
        } else {
            object2 = and__5236__auto__13987;
            and__5236__auto__13987 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            long start__13414__auto__13988 = System.nanoTime();
            Object object9 = v;
            v = null;
            Object ret__13415__auto__13989 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(this.db, (Object)attrid, object9));
            ((IFn.OLO)const__6.getRawRoot()).invokePrim(this.uniqueness, System.nanoTime() - start__13414__auto__13988);
            object = ret__13415__auto__13989;
            ret__13415__auto__13989 = null;
        } else {
            object = null;
        }
        return object;
    }
}

