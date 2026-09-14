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

public final class backup$require_latest_t
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"latest-t");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__2 = RT.keyword((String)"restore", (String)"no-roots");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"uri");

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object or__5238__auto__20249;
        Object object2 = or__5238__auto__20249 = ((IFn)const__0.getRawRoot()).invoke(uri2);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__20249;
            or__5238__auto__20249 = null;
        } else {
            Object object3 = ((IFn)const__3.getRawRoot()).invoke((Object)"No restore points available at ", uri2);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object4 = uri2;
            uri2 = null;
            objectArray[1] = object4;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$require_latest_t.invokeStatic(object2);
    }
}

