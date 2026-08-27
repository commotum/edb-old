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

public final class log$tail_ts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"txes");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__5 = RT.keyword(null, (String)"t");

    public static Object invokeStatic(Object p__16226) {
        Object txes;
        Object map__16227;
        Object object;
        Object object2 = p__16226;
        p__16226 = null;
        Object map__162272 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__162272);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__162272;
            map__162272 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__162272;
            map__162272 = null;
        }
        Object object5 = map__16227 = object;
        map__16227 = null;
        Object object6 = txes = RT.get((Object)object5, (Object)const__3);
        txes = null;
        return ((IFn)const__4.getRawRoot()).invoke((Object)const__5, object6);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$tail_ts.invokeStatic(object2);
    }
}

