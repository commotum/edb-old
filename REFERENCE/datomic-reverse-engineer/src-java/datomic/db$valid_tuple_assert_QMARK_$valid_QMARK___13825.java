/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class db$valid_tuple_assert_QMARK_$valid_QMARK___13825
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"tuple-value-types");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"instance?");
    public static final Keyword const__4 = RT.keyword((String)"db.type", (String)"ref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"string?");

    public Object invoke(Object kw, Object elem) {
        Object object;
        Object cl2;
        Object and__5236__auto__13830;
        Object object2 = and__5236__auto__13830 = (cl2 = ((IFn)const__0.getRawRoot()).invoke(kw));
        if (object2 != null && object2 != Boolean.FALSE) {
            boolean or__5238__auto__13829 = Util.identical((Object)elem, null);
            if (or__5238__auto__13829) {
                object = or__5238__auto__13829 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object or__5238__auto__13828;
                Object object3 = cl2;
                cl2 = null;
                Object object4 = or__5238__auto__13828 = ((IFn)const__2.getRawRoot()).invoke(object3, elem);
                if (object4 != null && object4 != Boolean.FALSE) {
                    object = or__5238__auto__13828;
                    or__5238__auto__13828 = null;
                } else {
                    Object object5 = kw;
                    kw = null;
                    boolean and__5236__auto__13827 = Util.equiv((Object)object5, (Object)const__4);
                    if (and__5236__auto__13827) {
                        Object object6 = elem;
                        elem = null;
                        db$valid_tuple_assert_QMARK_$valid_QMARK___13825 this_ = null;
                        object = ((IFn)const__5.getRawRoot()).invoke(object6);
                    } else {
                        object = and__5236__auto__13827 ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            }
        } else {
            object = and__5236__auto__13830;
            and__5236__auto__13830 = null;
        }
        return object;
    }
}

