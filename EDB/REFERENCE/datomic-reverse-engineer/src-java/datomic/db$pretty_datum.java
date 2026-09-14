/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
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
import datomic.db.Db;
import datomic.impl.db.IDatum;

public final class db$pretty_datum
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"p");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Keyword const__2 = RT.keyword(null, (String)"e");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__7 = RT.keyword((String)"db.type", (String)"ref");
    public static final Keyword const__8 = RT.keyword(null, (String)"t");
    public static final Keyword const__9 = RT.keyword(null, (String)"tx");
    public static final Keyword const__10 = RT.keyword(null, (String)"op");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object object2;
        Object or__5238__auto__14165;
        Object attr = ((Db)db2).elementAt(((IDatum)d).getA());
        Object[] objectArray = new Object[14];
        objectArray[0] = const__0;
        objectArray[1] = ((Db)db2).keywordOf(Numbers.num((long)((IFn.LL)const__1.getRawRoot()).invokePrim(((IDatum)d).getE())));
        objectArray[2] = const__2;
        Object object3 = or__5238__auto__14165 = ((Db)db2).keywordOf(Numbers.num((long)((IDatum)d).getE()));
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__14165;
            or__5238__auto__14165 = null;
        } else {
            object2 = Numbers.num((long)((IFn.LL)const__3.getRawRoot()).invokePrim(((IDatum)d).getE()));
        }
        objectArray[3] = object2;
        objectArray[4] = const__4;
        objectArray[5] = ((Db)db2).keywordOf(((IDatum)d).getA());
        objectArray[6] = const__5;
        Object object4 = attr;
        attr = null;
        if (Util.equiv((Object)((Attribute)object4).vtypeid, (Object)((Db)db2).idOf(const__7))) {
            Object or__5238__auto__14166;
            Object object5 = db2;
            db2 = null;
            Object object6 = or__5238__auto__14166 = ((Db)object5).keywordOf(((IDatum)d).getV());
            if (object6 != null && object6 != Boolean.FALSE) {
                object = or__5238__auto__14166;
                or__5238__auto__14166 = null;
            } else {
                object = ((IDatum)d).getV();
            }
        } else {
            object = ((IDatum)d).getV();
        }
        objectArray[7] = object;
        objectArray[8] = const__8;
        objectArray[9] = Numbers.num((long)((IDatum)d).getT());
        objectArray[10] = const__9;
        objectArray[11] = Numbers.num((long)((IDatum)d).getTx());
        objectArray[12] = const__10;
        Object object7 = d;
        d = null;
        objectArray[13] = ((IDatum)object7).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$pretty_datum.invokeStatic(object3, object4);
    }
}

