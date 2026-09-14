/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.math$mean_and_stddev$fn__482;

public final class math$mean_and_stddev
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.math", (String)"mean");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__5 = RT.keyword(null, (String)"mean");
    public static final Keyword const__6 = RT.keyword(null, (String)"stddev");

    public static Object invokeStatic(Object coll) {
        IPersistentMap iPersistentMap;
        Object object = ((IFn)const__0.getRawRoot()).invoke(coll);
        if (object != null && object != Boolean.FALSE) {
            Object m = ((IFn)const__1.getRawRoot()).invoke(coll);
            Object sos = ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke((Object)new math$mean_and_stddev$fn__482(m), coll));
            Object[] objectArray = new Object[4];
            objectArray[0] = const__5;
            Object object2 = m;
            m = null;
            objectArray[1] = object2;
            objectArray[2] = const__6;
            Object object3 = sos;
            sos = null;
            Object object4 = coll;
            coll = null;
            objectArray[3] = Math.sqrt(RT.doubleCast((Object)Numbers.divide((Object)object3, (long)RT.count((Object)object4))));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return math$mean_and_stddev.invokeStatic(object2);
    }
}

