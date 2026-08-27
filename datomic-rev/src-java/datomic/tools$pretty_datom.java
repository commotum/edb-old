/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Datom;

public final class tools$pretty_datom
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"e");
    public static final Keyword const__1 = RT.keyword(null, (String)"a");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Keyword const__3 = RT.keyword(null, (String)"v");
    public static final Keyword const__4 = RT.keyword(null, (String)"t");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"tx->t");
    public static final Keyword const__6 = RT.keyword(null, (String)"added");

    public static Object invokeStatic(Object db2, Object datom) {
        PersistentArrayMap persistentArrayMap;
        Object object = datom;
        if (object != null && object != Boolean.FALSE) {
            Object[] objectArray = new Object[10];
            objectArray[0] = const__0;
            objectArray[1] = ((Datom)datom).e();
            objectArray[2] = const__1;
            Object object2 = db2;
            db2 = null;
            objectArray[3] = ((IFn)const__2.getRawRoot()).invoke(object2, ((Datom)datom).a());
            objectArray[4] = const__3;
            objectArray[5] = ((Datom)datom).v();
            objectArray[6] = const__4;
            objectArray[7] = Numbers.num((long)((IFn.OL)const__5.getRawRoot()).invokePrim(((Datom)datom).tx()));
            objectArray[8] = const__6;
            Object object3 = datom;
            datom = null;
            objectArray[9] = ((Datom)object3).added() ? Boolean.TRUE : Boolean.FALSE;
            persistentArrayMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            persistentArrayMap = PersistentArrayMap.EMPTY;
        }
        return persistentArrayMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$pretty_datom.invokeStatic(object3, object4);
    }
}

