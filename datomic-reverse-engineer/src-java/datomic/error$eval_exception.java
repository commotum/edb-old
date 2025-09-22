/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class error$eval_exception
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"context");
    public static final Keyword const__4 = RT.keyword(null, (String)"expr");
    public static final Keyword const__5 = RT.keyword(null, (String)"arguments");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__9 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__10 = RT.keyword((String)"cognitect.anomalies", (String)"fault");
    public static final Keyword const__11 = RT.keyword((String)"cognitect.anomalies", (String)"message");
    public static final Keyword const__12 = RT.keyword((String)"datomic", (String)"eval-exception");

    public static Object invokeStatic(Object p__671, Object t) {
        Object msg;
        Object map__672;
        Object object;
        Object object2 = p__671;
        p__671 = null;
        Object map__6722 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__6722);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__6722;
            map__6722 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__6722;
            map__6722 = null;
        }
        Object cmap = map__672 = object;
        Object context = RT.get((Object)map__672, (Object)const__3);
        Object expr = RT.get((Object)map__672, (Object)const__4);
        Object object5 = map__672;
        map__672 = null;
        RT.get((Object)object5, (Object)const__5);
        Object object6 = context;
        context = null;
        Object object7 = expr;
        expr = null;
        Object object8 = msg = ((IFn)const__6.getRawRoot()).invoke((Object)"Error evaluating ", ((IFn)const__7.getRawRoot()).invoke(object6), (Object)": ", object7);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__9;
        objectArray[1] = const__10;
        objectArray[2] = const__11;
        Object object9 = msg;
        msg = null;
        objectArray[3] = object9;
        objectArray[4] = const__12;
        Object object10 = cmap;
        cmap = null;
        objectArray[5] = object10;
        Object object11 = t;
        t = null;
        return ((IFn)const__8.getRawRoot()).invoke(object8, (Object)RT.mapUniqueKeys((Object[])objectArray), object11);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return error$eval_exception.invokeStatic(object3, object4);
    }
}

