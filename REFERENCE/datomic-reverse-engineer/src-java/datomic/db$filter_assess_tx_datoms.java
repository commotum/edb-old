/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$filter_assess_tx_datoms$fn__13351;
import datomic.db$filter_assess_tx_datoms$fn__13358;
import datomic.db$filter_assess_tx_datoms$fn__13361;

public final class db$filter_assess_tx_datoms
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"create-schema-validator");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"create-deduper");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"create-op-validator");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"create-card-one-validator");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"create-unique-value-validator");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"create-cloud-compat-validator");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"filterv");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"sort");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"attrs-missing-hooks");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"new-ents-not-installed");
    public static final Var const__11 = RT.var((String)"datomic.error", (String)"argd");
    public static final Keyword const__12 = RT.keyword((String)"db.error", (String)"schema-without-install");
    public static final Keyword const__13 = RT.keyword(null, (String)"datoms");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"datom-error-desc");

    public static Object invokeStatic(Object db2, Object check_installs_QMARK_, Object datoms2) {
        Object result2;
        Object p0 = ((IFn)const__0.getRawRoot()).invoke(db2);
        Object p1 = ((IFn)const__1.getRawRoot()).invoke();
        Object p2 = ((IFn)const__2.getRawRoot()).invoke(db2);
        Object p3 = ((IFn)const__3.getRawRoot()).invoke(db2);
        Object p4 = ((IFn)const__4.getRawRoot()).invoke(db2);
        Object p5 = ((IFn)const__5.getRawRoot()).invoke(db2);
        Object object = p0;
        p0 = null;
        Object object2 = p5;
        p5 = null;
        Object object3 = p1;
        p1 = null;
        Object object4 = p4;
        p4 = null;
        Object object5 = p2;
        p2 = null;
        Object object6 = p3;
        p3 = null;
        Object object7 = datoms2;
        datoms2 = null;
        Object object8 = result2 = ((IFn)const__6.getRawRoot()).invoke((Object)new db$filter_assess_tx_datoms$fn__13351(object, object2, object3, object4, object5, object6), object7);
        Object object9 = result2;
        result2 = null;
        Object result3 = ((IFn)const__7.getRawRoot()).invoke((Object)new db$filter_assess_tx_datoms$fn__13358(db2), object8, ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(db2, object9)));
        Object object10 = check_installs_QMARK_;
        check_installs_QMARK_ = null;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object temp__5457__auto__13365;
            Object object11 = temp__5457__auto__13365 = ((IFn)const__10.getRawRoot()).invoke(db2, result3);
            if (object11 != null && object11 != Boolean.FALSE) {
                Object object12 = temp__5457__auto__13365;
                temp__5457__auto__13365 = null;
                Object es = object12;
                Object[] objectArray = new Object[2];
                objectArray[0] = const__13;
                Object object13 = db2;
                db2 = null;
                Object object14 = es;
                es = null;
                objectArray[1] = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(const__16.getRawRoot(), object13), ((IFn)const__6.getRawRoot()).invoke((Object)new db$filter_assess_tx_datoms$fn__13361(object14), result3));
                ((IFn)const__11.getRawRoot()).invoke((Object)const__12, (Object)"Only schema components can be installed in partition :db.part/db", (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        }
        Object object15 = result3;
        result3 = null;
        return object15;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$filter_assess_tx_datoms.invokeStatic(object4, object5, object6);
    }
}

