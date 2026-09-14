/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$require_id
extends AFunction
implements IFn.OOL {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"not-an-entity");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"entity");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"string-tempid?");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"datom-error-desc");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"drop");
    public static final Object const__8 = 1L;
    public static final Keyword const__9 = RT.keyword(null, (String)"datom");

    public static Object invokeStatic(Object db2, Object x, Object procargs) {
        Object object;
        Object or__5238__auto__12613;
        Object object2 = ((IFn)const__5.getRawRoot()).invoke(x);
        Object object3 = or__5238__auto__12613 = object2 != null && object2 != Boolean.FALSE ? x : null;
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__12613;
            or__5238__auto__12613 = null;
        } else {
            Object or__5238__auto__12612;
            Object object4 = or__5238__auto__12612 = ((IFn)const__0.getRawRoot()).invoke(db2, x);
            if (object4 != null && object4 != Boolean.FALSE) {
                object = or__5238__auto__12612;
                or__5238__auto__12612 = null;
            } else {
                Object object5 = ((IFn)const__3.getRawRoot()).invoke((Object)"Unable to resolve entity: ", x, (Object)" in datom ", ((IFn)const__6.getRawRoot()).invoke(db2, ((IFn)const__7.getRawRoot()).invoke(const__8, procargs)));
                Object[] objectArray = new Object[4];
                objectArray[0] = const__4;
                Object object6 = x;
                x = null;
                objectArray[1] = object6;
                objectArray[2] = const__9;
                Object object7 = db2;
                db2 = null;
                Object object8 = procargs;
                procargs = null;
                objectArray[3] = ((IFn)const__6.getRawRoot()).invoke(object7, ((IFn)const__7.getRawRoot()).invoke(const__8, object8));
                object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, object5, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$require_id.invokeStatic(object4, object5, object6);
    }

    public static long invokeStatic(Object db2, Object x) {
        Object object;
        Object or__5238__auto__12614;
        Object object2 = db2;
        db2 = null;
        Object object3 = or__5238__auto__12614 = ((IFn)const__0.getRawRoot()).invoke(object2, x);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__12614;
            or__5238__auto__12614 = null;
        } else {
            Object object4 = ((IFn)const__3.getRawRoot()).invoke((Object)"Unable to resolve entity: ", x);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object5 = x;
            x = null;
            objectArray[1] = object5;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return ((Number)object).longValue();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(db$require_id.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$require_id.invokeStatic(object3, object4);
    }
}

