/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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

public final class db$ensure_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"requiring-resolve!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"true?");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"entity-pred");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.set", (String)"map-invert");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"pred-return");

    public static Object invokeStatic(Object s, Object db2, Object e, Object spec, Object idmap) {
        Object object = s;
        s = null;
        Object pred2 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object result2 = ((IFn)pred2).invoke(db2, e);
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(result2);
        if (object2 == null || object2 == Boolean.FALSE) {
            Object object3 = idmap;
            idmap = null;
            Object object4 = e;
            Object object5 = e;
            e = null;
            Object object6 = pred2;
            pred2 = null;
            Object object7 = db2;
            db2 = null;
            Object object8 = spec;
            spec = null;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__8;
            Object object9 = result2;
            result2 = null;
            objectArray[1] = object9;
            throw (Throwable)((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"Entity ", RT.get((Object)((IFn)const__6.getRawRoot()).invoke(object3), (Object)object4, (Object)object5), (Object)" failed pred ", object6, (Object)" of spec ", ((IFn)const__7.getRawRoot()).invoke(object7, object8)), (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return db$ensure_pred.invokeStatic(object6, object7, object8, object9, object10);
    }
}

