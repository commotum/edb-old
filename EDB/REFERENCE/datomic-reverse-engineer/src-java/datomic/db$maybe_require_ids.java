/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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

public final class db$maybe_require_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Keyword const__2 = RT.keyword(null, (String)"else");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"system-eid");
    public static final Keyword const__8 = RT.keyword((String)"db.type", (String)"tuple");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"require-tuple-ids");
    public static final Keyword const__10 = RT.keyword(null, (String)"default");

    public static Object invokeStatic(Object db2, Object a, Object v, Object reverse_QMARK_) {
        Object object;
        Object object2 = reverse_QMARK_;
        reverse_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object and__5236__auto__12627;
            Object object3 = and__5236__auto__12627 = v;
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = db2;
                db2 = null;
                Object object5 = v;
                v = null;
                object = Numbers.num((long)((IFn.OOL)const__0.getRawRoot()).invokePrim(object4, object5));
            } else {
                object = and__5236__auto__12627;
                and__5236__auto__12627 = null;
            }
        } else {
            boolean or__5238__auto__12628 = Util.identical((Object)a, null);
            if (or__5238__auto__12628 ? or__5238__auto__12628 : Util.identical((Object)v, null)) {
                object = v;
                v = null;
            } else {
                Keyword keyword = const__2;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object aid;
                    Object object6 = a;
                    a = null;
                    Object object7 = aid = ((IFn)const__3.getRawRoot()).invoke(db2, object6);
                    aid = null;
                    Object attr = ((IFn)const__4.getRawRoot()).invoke(db2, object7);
                    Object vtypeid = ((Attribute)attr).vtypeid;
                    if (Util.equiv((long)20L, (Object)vtypeid)) {
                        Object object8 = db2;
                        db2 = null;
                        Object object9 = v;
                        v = null;
                        object = Numbers.num((long)((IFn.OOL)const__0.getRawRoot()).invokePrim(object8, object9));
                    } else {
                        Object object10 = vtypeid;
                        vtypeid = null;
                        if (Util.equiv((Object)((IFn)const__7.getRawRoot()).invoke(db2, (Object)const__8), (Object)object10)) {
                            Object object11 = db2;
                            db2 = null;
                            Object object12 = attr;
                            attr = null;
                            Object object13 = v;
                            v = null;
                            object = ((IFn)const__9.getRawRoot()).invoke(object11, object12, object13);
                        } else {
                            Keyword keyword2 = const__10;
                            if (keyword2 != null && keyword2 != Boolean.FALSE) {
                                object = v;
                                v = null;
                            } else {
                                object = null;
                            }
                        }
                    }
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$maybe_require_ids.invokeStatic(object5, object6, object7, object8);
    }
}

