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

public final class integrity$report_aevt_avet_stats
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__2 = RT.var((String)"datomic.integrity", (String)"aevt-avet-stats-consistent?");
    public static final Var const__3 = RT.var((String)"datomic.integrity", (String)"aevt-avet-stats");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"prn");
    public static final Keyword const__5 = RT.keyword(null, (String)"stats-mismatch");

    public static Object invokeStatic(Object db2) {
        Object object;
        Object temp__5457__auto__22492;
        Object object2 = db2;
        db2 = null;
        Object object3 = temp__5457__auto__22492 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(object2)));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__22492;
            temp__5457__auto__22492 = null;
            Object mismatch = object4;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__5;
            Object object5 = mismatch;
            mismatch = null;
            objectArray[1] = object5;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$report_aevt_avet_stats.invokeStatic(object2);
    }
}

