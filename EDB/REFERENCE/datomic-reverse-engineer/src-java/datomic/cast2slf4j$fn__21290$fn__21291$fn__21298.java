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

public final class cast2slf4j$fn__21290$fn__21291$fn__21298
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"name");
    public static final Keyword const__4 = RT.keyword(null, (String)"value");
    public static final Var const__5 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Var const__6 = RT.var((String)"datomic.cast2slf4j", (String)"cast-name->cloudwatch-name");

    public Object invoke(Object p__21297) {
        Object object;
        Object object2 = p__21297;
        p__21297 = null;
        Object map__21299 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__21299);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__21299;
            map__21299 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__21299;
            map__21299 = null;
        }
        Object map__212992 = object;
        Object name = RT.get((Object)map__212992, (Object)const__3);
        Object object5 = map__212992;
        map__212992 = null;
        Object value = RT.get((Object)object5, (Object)const__4);
        Object object6 = name;
        name = null;
        Object object7 = value;
        value = null;
        cast2slf4j$fn__21290$fn__21291$fn__21298 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object6), object7);
    }
}

