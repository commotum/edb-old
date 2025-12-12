/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class tools$rehome
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword((String)"db", (String)"retract");
    public static final Keyword const__7 = RT.keyword((String)"db", (String)"add");

    public static Object invokeStatic(Object p__21849, Object to) {
        Object object;
        Object object2 = p__21849;
        p__21849 = null;
        Object map__21850 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__21850);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__21850;
            map__21850 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__21850;
            map__21850 = null;
        }
        Object map__218502 = object;
        Object e = RT.get((Object)map__218502, (Object)const__3);
        Object a = RT.get((Object)map__218502, (Object)const__4);
        Object object5 = map__218502;
        map__218502 = null;
        Object v = RT.get((Object)object5, (Object)const__5);
        Object object6 = e;
        e = null;
        IPersistentVector iPersistentVector = Tuple.create((Object)const__6, (Object)object6, (Object)a, (Object)v);
        Object object7 = to;
        to = null;
        Object object8 = a;
        a = null;
        Object object9 = v;
        v = null;
        return Tuple.create((Object)iPersistentVector, (Object)Tuple.create((Object)const__7, (Object)object7, (Object)object8, (Object)object9));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$rehome.invokeStatic(object3, object4);
    }
}

