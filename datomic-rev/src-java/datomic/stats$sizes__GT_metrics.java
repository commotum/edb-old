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
import datomic.stats$sizes__GT_metrics$fn__17987;
import datomic.stats$sizes__GT_metrics$fn__17989;

public final class stats$sizes__GT_metrics
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"IndexSegments");
    public static final Var const__1 = RT.var((String)"datomic.stats", (String)"segment-count");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__3 = RT.keyword(null, (String)"FulltextSegments");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"filter");
    public static final Keyword const__5 = RT.keyword(null, (String)"Datoms");
    public static final Var const__6 = RT.var((String)"datomic.stats", (String)"datom-count");
    public static final Keyword const__7 = RT.keyword(null, (String)"IndexDatoms");
    public static final Var const__8 = RT.var((String)"datomic.stats", (String)"index-datom-count");

    public static Object invokeStatic(Object s) {
        Object[] objectArray = new Object[8];
        objectArray[0] = const__0;
        objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new stats$sizes__GT_metrics$fn__17987(), s));
        objectArray[2] = const__3;
        objectArray[3] = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)new stats$sizes__GT_metrics$fn__17989(), s));
        objectArray[4] = const__5;
        objectArray[5] = ((IFn)const__6.getRawRoot()).invoke(s);
        objectArray[6] = const__7;
        Object object = s;
        s = null;
        objectArray[7] = ((IFn)const__8.getRawRoot()).invoke(object);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$sizes__GT_metrics.invokeStatic(object2);
    }
}

