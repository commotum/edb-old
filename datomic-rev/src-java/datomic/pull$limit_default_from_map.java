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

public final class pull$limit_default_from_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"limit");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__6 = RT.var((String)"datomic.pull", (String)"default-limit");

    public static Object invokeStatic(Object p__18944) {
        Object object;
        Object map__18945;
        Object object2;
        Object object3 = p__18944;
        p__18944 = null;
        Object map__189452 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__189452);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__189452;
            map__189452 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__189452;
            map__189452 = null;
        }
        Object args = map__18945 = object2;
        Object object6 = map__18945;
        map__18945 = null;
        Object limit2 = RT.get((Object)object6, (Object)const__3);
        Object object7 = args;
        args = null;
        Object object8 = ((IFn)const__4.getRawRoot()).invoke(object7, (Object)const__3);
        if (object8 != null && object8 != Boolean.FALSE) {
            object = limit2;
            limit2 = null;
        } else {
            object = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot());
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$limit_default_from_map.invokeStatic(object2);
    }
}

