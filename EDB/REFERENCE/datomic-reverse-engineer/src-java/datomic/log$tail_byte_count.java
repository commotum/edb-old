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

public final class log$tail_byte_count
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"bufs");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__7 = RT.var((String)"datomic.io", (String)"remaining");

    public static Object invokeStatic(Object p__16223) {
        Object bufs;
        Object map__16224;
        Object object;
        Object object2 = p__16223;
        p__16223 = null;
        Object map__162242 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__162242);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__162242;
            map__162242 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__162242;
            map__162242 = null;
        }
        Object object5 = map__16224 = object;
        map__16224 = null;
        Object object6 = bufs = RT.get((Object)object5, (Object)const__3);
        bufs = null;
        return ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), object6));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$tail_byte_count.invokeStatic(object2);
    }
}

