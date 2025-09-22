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
import datomic.pull$pull_1$f__19054;

public final class pull$pull_1
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"pull-1");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"io-context");
    public static final Var const__5 = RT.var((String)"datomic.measure.io-stats", (String)"throw-if-ex!");
    public static final Var const__6 = RT.var((String)"datomic.measure.io-stats", (String)"with-io-stats");
    public static final Keyword const__7 = RT.keyword(null, (String)"api");
    public static final Keyword const__8 = RT.keyword(null, (String)"pull");

    public static Object invokeStatic(Object db2, Object selector, Object e, Object p__19052) {
        Object object;
        Object object2;
        Object object3 = p__19052;
        p__19052 = null;
        Object map__19053 = object3;
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__19053);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__19053;
            map__19053 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__19053;
            map__19053 = null;
        }
        Object map__190532 = object2;
        Object object6 = map__190532;
        map__190532 = null;
        Object io_context = RT.get((Object)object6, (Object)const__4);
        Object object7 = db2;
        db2 = null;
        Object object8 = selector;
        selector = null;
        Object object9 = e;
        e = null;
        pull$pull_1$f__19054 f = new pull$pull_1$f__19054(object7, object8, object9);
        Object object10 = io_context;
        if (object10 != null && object10 != Boolean.FALSE) {
            pull$pull_1$f__19054 pull$pull_1$f__19054 = f;
            f = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__4;
            Object object11 = io_context;
            io_context = null;
            objectArray[1] = object11;
            objectArray[2] = const__7;
            objectArray[3] = const__8;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)pull$pull_1$f__19054, (Object)RT.mapUniqueKeys((Object[])objectArray)));
        } else {
            pull$pull_1$f__19054 pull$pull_1$f__19054 = f;
            f = null;
            object = ((IFn)pull$pull_1$f__19054).invoke();
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return pull$pull_1.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object db2, Object selector, Object e) {
        Object object = db2;
        db2 = null;
        Object object2 = selector;
        selector = null;
        Object object3 = e;
        e = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, null);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return pull$pull_1.invokeStatic(object4, object5, object6);
    }
}

