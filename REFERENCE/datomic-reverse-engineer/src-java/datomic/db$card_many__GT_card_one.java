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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db$card_many__GT_card_one
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"card-one-violator");
    public static final Keyword const__1 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"cardinality-violation");
    public static final Keyword const__3 = RT.keyword(null, (String)"datoms");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"set-element-fields");
    public static final Keyword const__5 = RT.keyword(null, (String)"cardinality");
    public static final Object const__6 = 35L;

    public static Object invokeStatic(Object db2, Object aid, Object _, Object _2) {
        IPersistentVector iPersistentVector;
        Object temp__5455__auto__13152;
        Object object = temp__5455__auto__13152 = ((IFn)const__0.getRawRoot()).invoke(db2, aid);
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5455__auto__13152;
            temp__5455__auto__13152 = null;
            Object problem = object2;
            Object object3 = db2;
            db2 = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            objectArray[1] = const__2;
            objectArray[2] = const__3;
            Object object4 = problem;
            problem = null;
            objectArray[3] = object4;
            iPersistentVector = Tuple.create((Object)object3, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
        } else {
            Object object5 = db2;
            db2 = null;
            Object object6 = aid;
            aid = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__4.getRawRoot()).invoke(object5, object6, (Object)const__5, const__6));
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
        return db$card_many__GT_card_one.invokeStatic(object5, object6, object7, object8);
    }
}

