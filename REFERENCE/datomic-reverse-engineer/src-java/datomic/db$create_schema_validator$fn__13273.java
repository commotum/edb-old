/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.db.Attribute;
import datomic.impl.db.IDatum;

public final class db$create_schema_validator$fn__13273
extends AFunction {
    Object db;
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"get-part");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"find-eavt");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"system-schema-datom?");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"datom-error-desc");
    public static final Var const__13 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__14 = RT.keyword((String)"db.error", (String)"datom-cannot-be-altered");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__16 = RT.keyword(null, (String)"datom");

    public db$create_schema_validator$fn__13273(Object object) {
        this.db = object;
    }

    public Object invoke(Object d) {
        Object object;
        if (((IFn.OL)const__1.getRawRoot()).invokePrim(d) == 0L) {
            if (((IDatum)d).getT() == 0L) {
                object = Boolean.TRUE;
            } else {
                int a = ((IDatum)d).getA();
                boolean or__5238__auto__13275 = Util.equiv((long)a, (long)13L);
                if (or__5238__auto__13275 ? or__5238__auto__13275 : Util.equiv((long)a, (long)19L)) {
                    object = Boolean.FALSE;
                } else {
                    Object temp__5455__auto__13279;
                    Object object2 = temp__5455__auto__13279 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(this_.db, (Object)Numbers.num((long)((IDatum)d).getE()), (Object)((IDatum)d).getA()));
                    if (object2 != null && object2 != Boolean.FALSE) {
                        Object object3 = temp__5455__auto__13279;
                        temp__5455__auto__13279 = null;
                        Object existing = object3;
                        boolean and__5236__auto__13276 = Numbers.isZero((long)((IFn.OOL)const__7.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)existing).getV()));
                        if (and__5236__auto__13276 ? Util.equiv((boolean)((IDatum)d).isAssertion(), (boolean)((IDatum)existing).isAssertion()) : and__5236__auto__13276) {
                            object = Boolean.FALSE;
                        } else {
                            Object object4;
                            Object and__5236__auto__13278;
                            Object object5 = existing;
                            existing = null;
                            Object object6 = and__5236__auto__13278 = ((IFn)const__8.getRawRoot()).invoke(this_.db, object5);
                            if (object6 != null && object6 != Boolean.FALSE) {
                                boolean or__5238__auto__13277 = Util.equiv((Object)((Attribute)((IFn)db$create_schema_validator$fn__13273.const__9.getRawRoot()).invoke((Object)this_.db, (Object)Integer.valueOf((int)a))).cardinality, (long)35L);
                                object4 = or__5238__auto__13277 ? (or__5238__auto__13277 ? Boolean.TRUE : Boolean.FALSE) : ((IFn)const__11.getRawRoot()).invoke((Object)(((IDatum)d).isAssertion() ? Boolean.TRUE : Boolean.FALSE));
                            } else {
                                object4 = and__5236__auto__13278;
                                and__5236__auto__13278 = null;
                            }
                            if (object4 != null && object4 != Boolean.FALSE) {
                                Object object7 = d;
                                d = null;
                                Object m = ((IFn)const__12.getRawRoot()).invoke(this_.db, object7);
                                Object object8 = ((IFn)const__15.getRawRoot()).invoke((Object)"Boot datoms cannot be altered: ", m);
                                Object[] objectArray = new Object[2];
                                objectArray[0] = const__16;
                                Object object9 = m;
                                m = null;
                                objectArray[1] = object9;
                                db$create_schema_validator$fn__13273 this_ = null;
                                object = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, object8, (Object)RT.mapUniqueKeys((Object[])objectArray));
                            } else {
                                object = Boolean.TRUE;
                            }
                        }
                    } else {
                        object = Boolean.TRUE;
                    }
                }
            }
        } else {
            object = Boolean.TRUE;
        }
        return object;
    }
}

