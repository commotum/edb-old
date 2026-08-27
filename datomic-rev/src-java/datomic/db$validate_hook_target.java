/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$validate_hook_target
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-datom");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"a");
    public static final Keyword const__6 = RT.keyword(null, (String)"e");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"not-in-system-partition");
    public static final Keyword const__9 = RT.keyword(null, (String)"v");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object a;
        if (((IDatum)d).getE() == 0L) {
        } else {
            a = ((IFn)const__1.getRawRoot()).invoke(db2, (Object)((IDatum)d).getA());
            Object e = ((IFn)const__1.getRawRoot()).invoke(db2, (Object)Numbers.num((long)((IDatum)d).getE()));
            Object object2 = a;
            a = null;
            Object object3 = ((IFn)const__4.getRawRoot()).invoke(object2, (Object)" must be set on entity :db.part/db, found ", e);
            Object[] objectArray = new Object[4];
            objectArray[0] = const__5;
            objectArray[1] = e;
            objectArray[2] = const__6;
            Object object4 = e;
            e = null;
            objectArray[3] = object4;
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        if (((IFn.LL)const__7.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)((IDatum)d).getV()))) == 0L) {
            object = null;
        } else {
            a = ((IFn)const__1.getRawRoot()).invoke(db2, (Object)((IDatum)d).getA());
            Object object5 = db2;
            db2 = null;
            Object object6 = d;
            d = null;
            Object v = ((IFn)const__1.getRawRoot()).invoke(object5, ((IDatum)object6).getV());
            Object object7 = ((IFn)const__4.getRawRoot()).invoke((Object)"Value of ", a, (Object)" must be in :db.part/db partition, found ", v);
            Object[] objectArray = new Object[4];
            objectArray[0] = const__5;
            Object object8 = a;
            a = null;
            objectArray[1] = object8;
            objectArray[2] = const__9;
            Object object9 = v;
            v = null;
            objectArray[3] = object9;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__8, object7, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$validate_hook_target.invokeStatic(object3, object4);
    }
}

