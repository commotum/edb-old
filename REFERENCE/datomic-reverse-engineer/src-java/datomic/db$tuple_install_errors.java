/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$tuple_install_errors$fn__13063;
import datomic.db$tuple_install_errors$fn__13065;
import datomic.db$tuple_install_errors$fn__13067;

public final class db$tuple_install_errors
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"db", (String)"tupleType");
    public static final Keyword const__2 = RT.keyword((String)"db", (String)"tupleTypes");
    public static final Keyword const__3 = RT.keyword((String)"db", (String)"tupleAttrs");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"system-eid");
    public static final Keyword const__5 = RT.keyword((String)"db.type", (String)"tuple");
    public static final Keyword const__7 = RT.keyword((String)"db", (String)"valueType");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Keyword const__14 = RT.keyword((String)"db.error", (String)"invalid-tuple-kind");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"tuple-value-types");
    public static final Keyword const__18 = RT.keyword((String)"db.error", (String)"invalid-tuple-type");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"not-every?");
    public static final Keyword const__20 = RT.keyword((String)"db.error", (String)"invalid-tuple-types");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__22 = 35L;
    public static final Keyword const__23 = RT.keyword((String)"db", (String)"cardinality");
    public static final Keyword const__24 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__25 = RT.keyword((String)"db.error", (String)"tuple-of-attrs-must-be-card-one");
    public static final Keyword const__26 = RT.keyword(null, (String)"entity");
    public static final Keyword const__27 = RT.keyword((String)"db.error", (String)"invalid-tuple-attrs");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"<=");
    public static final Object const__30 = 2L;
    public static final Object const__31 = 8L;
    public static final Keyword const__32 = RT.keyword((String)"db.error", (String)"invalid-tuple-length");
    public static final Keyword const__33 = RT.keyword((String)"db.error", (String)"type-on-non-tuple");

    public static Object invokeStatic(Object db2, Object e) {
        Object object;
        Object temp__5457__auto__13077;
        Object type = RT.get((Object)e, (Object)const__1);
        Object types = RT.get((Object)e, (Object)const__2);
        Object attrs = RT.get((Object)e, (Object)const__3);
        Object object2 = temp__5457__auto__13077 = ((IFn)const__4.getRawRoot()).invoke(db2, (Object)const__5);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object TYPE_TUPLE;
            Object object3 = temp__5457__auto__13077;
            temp__5457__auto__13077 = null;
            Object object4 = TYPE_TUPLE = object3;
            TYPE_TUPLE = null;
            if (Util.equiv((Object)object4, (Object)RT.get((Object)e, (Object)const__7))) {
                Object object5;
                Object and__5236__auto__13074;
                IPersistentMap iPersistentMap;
                Object object6;
                Object and__5236__auto__13073;
                IPersistentMap iPersistentMap2;
                Object object7;
                Object and__5236__auto__13072;
                Object object8;
                Object and__5236__auto__13071;
                Object object9;
                Object and__5236__auto__13070;
                IFn iFn = (IFn)const__8.getRawRoot();
                IFn iFn2 = (IFn)const__9.getRawRoot();
                db$tuple_install_errors$fn__13063 db$tuple_install_errors$fn__13063 = new db$tuple_install_errors$fn__13063(e);
                IFn iFn3 = (IFn)const__10.getRawRoot();
                Object object10 = const__11.getRawRoot();
                Keyword keyword = 1L == (long)RT.count((Object)((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), (Object)Tuple.create((Object)type, (Object)types, (Object)attrs))) ? null : const__14;
                Object object11 = and__5236__auto__13070 = type;
                if (object11 != null && object11 != Boolean.FALSE) {
                    Object object12 = type;
                    type = null;
                    object9 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot(), object12));
                } else {
                    object9 = and__5236__auto__13070;
                    and__5236__auto__13070 = null;
                }
                Keyword keyword2 = object9 != null && object9 != Boolean.FALSE ? const__18 : null;
                Object object13 = and__5236__auto__13071 = types;
                if (object13 != null && object13 != Boolean.FALSE) {
                    object8 = ((IFn)const__19.getRawRoot()).invoke(const__17.getRawRoot(), types);
                } else {
                    object8 = and__5236__auto__13071;
                    and__5236__auto__13071 = null;
                }
                Keyword keyword3 = object8 != null && object8 != Boolean.FALSE ? const__20 : null;
                Object object14 = and__5236__auto__13072 = attrs;
                if (object14 != null && object14 != Boolean.FALSE) {
                    object7 = ((IFn)const__21.getRawRoot()).invoke(const__22, RT.get((Object)e, (Object)const__23));
                } else {
                    object7 = and__5236__auto__13072;
                    and__5236__auto__13072 = null;
                }
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__24;
                    objectArray[1] = const__25;
                    objectArray[2] = const__26;
                    Object object15 = e;
                    e = null;
                    objectArray[3] = object15;
                    iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
                } else {
                    iPersistentMap2 = null;
                }
                Object object16 = and__5236__auto__13073 = attrs;
                if (object16 != null && object16 != Boolean.FALSE) {
                    object6 = ((IFn)const__19.getRawRoot()).invoke((Object)new db$tuple_install_errors$fn__13065(db2), attrs);
                } else {
                    object6 = and__5236__auto__13073;
                    and__5236__auto__13073 = null;
                }
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__24;
                    objectArray[1] = const__27;
                    objectArray[2] = const__3;
                    Object object17 = db2;
                    db2 = null;
                    Object object18 = attrs;
                    attrs = null;
                    objectArray[3] = ((IFn)const__28.getRawRoot()).invoke((Object)new db$tuple_install_errors$fn__13067(object17), object18);
                    iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
                } else {
                    iPersistentMap = null;
                }
                Object object19 = and__5236__auto__13074 = types;
                if (object19 != null && object19 != Boolean.FALSE) {
                    Object object20 = types;
                    types = null;
                    object5 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__29.getRawRoot()).invoke(const__30, (Object)RT.count((Object)object20), const__31));
                } else {
                    object5 = and__5236__auto__13074;
                    and__5236__auto__13074 = null;
                }
                object = iFn.invoke(iFn2.invoke((Object)db$tuple_install_errors$fn__13063, iFn3.invoke(object10, (Object)Tuple.create((Object)keyword, (Object)keyword2, (Object)keyword3, (Object)iPersistentMap2, iPersistentMap, (Object)(object5 != null && object5 != Boolean.FALSE ? const__32 : null)))));
            } else {
                Object object21;
                Object or__5238__auto__13076;
                Object object22 = type;
                type = null;
                Object object23 = or__5238__auto__13076 = object22;
                if (object23 != null && object23 != Boolean.FALSE) {
                    object21 = or__5238__auto__13076;
                    or__5238__auto__13076 = null;
                } else {
                    Object or__5238__auto__13075;
                    Object object24 = types;
                    types = null;
                    Object object25 = or__5238__auto__13075 = object24;
                    if (object25 != null && object25 != Boolean.FALSE) {
                        object21 = or__5238__auto__13075;
                        or__5238__auto__13075 = null;
                    } else {
                        object21 = attrs;
                        attrs = null;
                    }
                }
                if (object21 != null && object21 != Boolean.FALSE) {
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__24;
                    objectArray[1] = const__33;
                    objectArray[2] = const__26;
                    Object object26 = e;
                    e = null;
                    objectArray[3] = object26;
                    object = RT.mapUniqueKeys((Object[])objectArray);
                } else {
                    object = null;
                }
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$tuple_install_errors.invokeStatic(object3, object4);
    }
}

