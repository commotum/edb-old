/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class db$add_unique
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Keyword const__3 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"unique-not-allowed");
    public static final Keyword const__5 = RT.keyword(null, (String)"attribute");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"unique-violator");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"unique-violation");
    public static final Keyword const__9 = RT.keyword(null, (String)"datoms");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"set-element-fields");
    public static final Keyword const__11 = RT.keyword(null, (String)"unique");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"can-immediately-toggle-storage-has-avet?");
    public static final Keyword const__13 = RT.keyword(null, (String)"storageHasAVET");
    public static final Keyword const__14 = RT.keyword(null, (String)"needsAVET");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"unique-without-index");

    public static Object invokeStatic(Object db2, Object aid, Object _, Object unique) {
        IPersistentVector iPersistentVector;
        Object attr = ((IFn)const__0.getRawRoot()).invoke(db2, aid);
        if (Util.equiv((long)27L, (Object)((Attribute)attr).vtypeid)) {
            Object object = db2;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__3;
            objectArray[1] = const__4;
            objectArray[2] = const__5;
            Object object2 = db2;
            db2 = null;
            Object object3 = aid;
            aid = null;
            objectArray[3] = ((IFn)const__6.getRawRoot()).invoke(object2, object3);
            iPersistentVector = Tuple.create((Object)object, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
        } else {
            Object object = attr;
            attr = null;
            Object object4 = ((Attribute)object).hasAVET();
            if (object4 != null && object4 != Boolean.FALSE) {
                Object temp__5455__auto__13163;
                Object object5 = temp__5455__auto__13163 = ((IFn)const__7.getRawRoot()).invoke(db2, aid);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = temp__5455__auto__13163;
                    temp__5455__auto__13163 = null;
                    Object problem = object6;
                    Object object7 = db2;
                    db2 = null;
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__3;
                    objectArray[1] = const__8;
                    objectArray[2] = const__9;
                    Object object8 = problem;
                    problem = null;
                    objectArray[3] = object8;
                    iPersistentVector = Tuple.create((Object)object7, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
                } else {
                    Object object9 = db2;
                    db2 = null;
                    Object object10 = aid;
                    aid = null;
                    Object object11 = unique;
                    unique = null;
                    iPersistentVector = Tuple.create((Object)((IFn)const__10.getRawRoot()).invoke(object9, object10, (Object)const__11, object11));
                }
            } else {
                Object object12 = ((IFn)const__12.getRawRoot()).invoke(db2, aid);
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object object13 = db2;
                    db2 = null;
                    Object object14 = aid;
                    aid = null;
                    Object object15 = unique;
                    unique = null;
                    iPersistentVector = Tuple.create((Object)((IFn)const__10.getRawRoot()).invoke(object13, object14, (Object)const__13, (Object)Boolean.TRUE, (Object)const__14, (Object)Boolean.TRUE, (Object)const__11, object15));
                } else {
                    Object object16 = db2;
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__3;
                    objectArray[1] = const__15;
                    objectArray[2] = const__5;
                    Object object17 = db2;
                    db2 = null;
                    Object object18 = aid;
                    aid = null;
                    objectArray[3] = ((IFn)const__6.getRawRoot()).invoke(object17, object18);
                    iPersistentVector = Tuple.create((Object)object16, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
                }
            }
        }
        return iPersistentVector;
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
        return db$add_unique.invokeStatic(object5, object6, object7, object8);
    }
}

