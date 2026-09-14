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

public final class integrity$validate_nohistory
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"cauterize");
    public static final Var const__1 = RT.var((String)"datomic.api", (String)"history");
    public static final Keyword const__2 = RT.keyword(null, (String)"memidx");
    public static final Keyword const__3 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__4 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__5 = RT.keyword(null, (String)"index");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__7 = RT.var((String)"datomic.integrity", (String)"attr-datoms");
    public static final Var const__8 = RT.var((String)"datomic.integrity", (String)"nohistory-attrs");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__10 = RT.keyword(null, (String)"datoms");

    public static Object invokeStatic(Object db2) {
        Object temp__5455__auto__22470;
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(db2), (Object)const__2, (Object)const__3, (Object)const__4, (Object)const__5);
        Object object = db2;
        Object object2 = db2;
        db2 = null;
        Object object3 = temp__5455__auto__22470 = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object, ((IFn)const__8.getRawRoot()).invoke(object2)));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__22470;
            temp__5455__auto__22470 = null;
            Object datoms2 = object4;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__10;
            Object object5 = datoms2;
            datoms2 = null;
            objectArray[1] = object5;
            throw (Throwable)((IFn)const__9.getRawRoot()).invoke((Object)"Found :db/noHistory datoms in the history index", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_nohistory.invokeStatic(object2);
    }
}

