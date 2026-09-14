/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$OL
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.impl.db.IDatum;

public final class db$has_tx_inst_QMARK_$fn__13893
extends AFunction {
    Object now;
    Object db;
    public static final Object const__1 = 50L;
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"get-part");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"multiple-tx-instants");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"get-eidx");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"reset-tx-instant");
    public static final Var const__10 = RT.var((String)"datomic.common", (String)"compare");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"future-tx-instant");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"find-eavt");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"past-tx-instant");

    public db$has_tx_inst_QMARK_$fn__13893(Object object, Object object2) {
        this.now = object;
        this.db = object2;
    }

    /*
     * WARNING - void declaration
     */
    public Object invoke(Object result2, Object d) {
        Object object;
        void var3_3;
        boolean and__5236__auto__13895 = Util.equiv((long)50L, (long)((IDatum)d).getA());
        if (and__5236__auto__13895 ? Util.equiv((long)3L, (long)((IFn.OL)const__3.getRawRoot()).invokePrim(d)) : var3_3) {
            Object temp__5457__auto__13896;
            long basis = ((Database)this.db).nextT();
            Object v = ((IDatum)d).getV();
            Object object2 = result2;
            result2 = null;
            if (object2 != null && object2 != Boolean.FALSE) {
                ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__6.getRawRoot()).invoke((Object)"Time conflict: :db/txInstant specified more than once"));
            }
            Object object3 = d;
            d = null;
            if (basis == ((IFn.OL)const__7.getRawRoot()).invokePrim(object3)) {
            } else {
                ((IFn)const__4.getRawRoot()).invoke((Object)const__8, ((IFn)const__6.getRawRoot()).invoke((Object)"You can set :db/txInstant only on the current transaction."));
            }
            if (((IFn.OOL)const__10.getRawRoot()).invokePrim(this.now, v) < 0L) {
                ((IFn)const__4.getRawRoot()).invoke((Object)const__11, ((IFn)const__6.getRawRoot()).invoke((Object)"Time conflict: ", v, (Object)" is in the future"));
            }
            Object object4 = temp__5457__auto__13896 = ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(this.db, (Object)Numbers.num((long)((IFn.LLL)const__14.getRawRoot()).invokePrim(3L, ((Database)this.db).basisT())), const__1));
            if (object4 != null && object4 != Boolean.FALSE) {
                Object basis_inst;
                Object object5 = temp__5457__auto__13896;
                temp__5457__auto__13896 = null;
                Object object6 = basis_inst = object5;
                basis_inst = null;
                if (((IFn.OOL)const__10.getRawRoot()).invokePrim(v, ((IDatum)object6).getV()) < 0L) {
                    Object object7 = v;
                    v = null;
                    ((IFn)const__4.getRawRoot()).invoke((Object)const__15, ((IFn)const__6.getRawRoot()).invoke((Object)"Time conflict: ", object7, (Object)" is older than database basis"));
                }
            }
            object = Boolean.TRUE;
        } else {
            object = result2;
            Object var1_1 = null;
        }
        return object;
    }
}

