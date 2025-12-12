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

public final class catalog$pod__GT_catalog
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"buf");
    public static final Keyword const__4 = RT.keyword(null, (String)"rev");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__6 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__7 = RT.var((String)"datomic.io", (String)"bbuf->string");
    public static final Keyword const__8 = RT.keyword((String)"datomic", (String)"rev");

    public static Object invokeStatic(Object pod2) {
        Object object;
        Object object2 = pod2;
        pod2 = null;
        Object map__11075 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__11075);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__11075;
            map__11075 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__11075;
            map__11075 = null;
        }
        Object map__110752 = object;
        Object buf = RT.get((Object)map__110752, (Object)const__3);
        Object object5 = map__110752;
        map__110752 = null;
        Object rev = RT.get((Object)object5, (Object)const__4);
        Object object6 = buf;
        buf = null;
        Object object7 = rev;
        rev = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object6)), (Object)const__8, object7);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return catalog$pod__GT_catalog.invokeStatic(object2);
    }
}

