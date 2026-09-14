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

public final class log$extend_tail
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"txes");
    public static final Keyword const__4 = RT.keyword(null, (String)"bufs");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"create-tail");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into");

    public static Object invokeStatic(Object p__16232, Object new_txes, Object new_bufs) {
        Object object;
        Object object2 = p__16232;
        p__16232 = null;
        Object map__16233 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__16233);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__16233;
            map__16233 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__16233;
            map__16233 = null;
        }
        Object map__162332 = object;
        Object txes = RT.get((Object)map__162332, (Object)const__3);
        Object object5 = map__162332;
        map__162332 = null;
        Object bufs = RT.get((Object)object5, (Object)const__4);
        Object object6 = txes;
        txes = null;
        Object object7 = new_txes;
        new_txes = null;
        Object object8 = bufs;
        bufs = null;
        Object object9 = new_bufs;
        new_bufs = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object6, object7), ((IFn)const__6.getRawRoot()).invoke(object8, object9));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$extend_tail.invokeStatic(object4, object5, object6);
    }
}

