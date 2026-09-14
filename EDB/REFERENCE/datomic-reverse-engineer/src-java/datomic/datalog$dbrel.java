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
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog.DbRel;
import datomic.db.Attribute;

public final class datalog$dbrel
extends AFunction {
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__11 = RT.var((String)"datomic.datalog", (String)"resolve-id");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Keyword const__13 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object db2, Object p__18171, Object starts, Object whiles) {
        Object object;
        Object object2;
        Object and__5236__auto__18179;
        Object object3;
        Object attr;
        Object and__5236__auto__18178;
        Object object4;
        Object attrid;
        Object and__5236__auto__18177;
        Object object5;
        Object and__5236__auto__18176;
        Object object6 = p__18171;
        p__18171 = null;
        Object vec__18172 = object6;
        Object e = RT.nth((Object)vec__18172, (int)RT.uncheckedIntCast((long)0L), null);
        Object a = RT.nth((Object)vec__18172, (int)RT.uncheckedIntCast((long)1L), null);
        Object v = RT.nth((Object)vec__18172, (int)RT.uncheckedIntCast((long)2L), null);
        Object t = RT.nth((Object)vec__18172, (int)RT.uncheckedIntCast((long)3L), null);
        Object object7 = vec__18172;
        vec__18172 = null;
        Object added = RT.nth((Object)object7, (int)RT.uncheckedIntCast((long)4L), null);
        Object object8 = and__5236__auto__18176 = a;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = a;
            a = null;
            object5 = Numbers.num((long)((IFn.OOL)const__6.getRawRoot()).invokePrim(db2, object9));
        } else {
            object5 = and__5236__auto__18176;
            and__5236__auto__18176 = null;
        }
        Object object10 = and__5236__auto__18177 = (attrid = object5);
        if (object10 != null && object10 != Boolean.FALSE) {
            object4 = ((IFn)const__7.getRawRoot()).invoke(db2, attrid);
        } else {
            object4 = and__5236__auto__18177;
            and__5236__auto__18177 = null;
        }
        Object object11 = and__5236__auto__18178 = (attr = object4);
        if (object11 != null && object11 != Boolean.FALSE) {
            object3 = Util.equiv((long)20L, (Object)((Attribute)attr).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object3 = and__5236__auto__18178;
            and__5236__auto__18178 = null;
        }
        Object ref_QMARK_ = object3;
        Object object12 = and__5236__auto__18179 = attr;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object object13 = attr;
            attr = null;
            object2 = Util.equiv((long)21L, (Object)((Attribute)object13).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__18179;
            and__5236__auto__18179 = null;
        }
        Object key_QMARK_ = object2;
        Object object14 = ref_QMARK_;
        Object object15 = e;
        e = null;
        Object object16 = ((IFn)const__11.getRawRoot()).invoke(db2, object15);
        Object object17 = attrid;
        attrid = null;
        Object object18 = ref_QMARK_;
        ref_QMARK_ = null;
        if (object18 != null && object18 != Boolean.FALSE) {
            Object object19 = db2;
            db2 = null;
            Object object20 = v;
            v = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object19, object20);
        } else {
            Object object21 = key_QMARK_;
            key_QMARK_ = null;
            if (object21 != null && object21 != Boolean.FALSE) {
                Object object22 = v;
                v = null;
                object = ((IFn)const__12.getRawRoot()).invoke(object22);
            } else {
                Keyword keyword = const__13;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = v;
                    v = null;
                } else {
                    object = null;
                }
            }
        }
        Object object23 = t;
        t = null;
        Object object24 = added;
        added = null;
        Object object25 = starts;
        starts = null;
        Object object26 = whiles;
        whiles = null;
        return new DbRel(db2, object14, key_QMARK_, Tuple.create((Object)object16, (Object)object17, (Object)object, (Object)object23, (Object)object24), object25, object26);
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
        return datalog$dbrel.invokeStatic(object5, object6, object7, object8);
    }
}

