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
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class pull$attr_with_opts__GT_attr_tuple
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Keyword const__7 = RT.keyword(null, (String)"as");
    public static final Var const__8 = RT.var((String)"datomic.pull", (String)"limit-default-from-map");
    public static final Var const__9 = RT.var((String)"datomic.pull", (String)"attr-with-opts->valfn");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"constantly");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"identity");

    public static Object invokeStatic(Object p__18959) {
        Object object;
        Object map__18963;
        Object object2;
        Object vec__18960;
        Object object3 = p__18959;
        p__18959 = null;
        Object object4 = vec__18960 = object3;
        vec__18960 = null;
        Object seq__18961 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object first__18962 = ((IFn)const__1.getRawRoot()).invoke(seq__18961);
        Object object5 = seq__18961;
        seq__18961 = null;
        Object seq__189612 = ((IFn)const__2.getRawRoot()).invoke(object5);
        Object object6 = first__18962;
        first__18962 = null;
        Object attr = object6;
        Object object7 = seq__189612;
        seq__189612 = null;
        Object args = object7;
        Object map__189632 = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), args);
        Object object8 = ((IFn)const__5.getRawRoot()).invoke(map__189632);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__189632;
            map__189632 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__0.getRawRoot()).invoke(object9)));
        } else {
            object2 = map__189632;
            map__189632 = null;
        }
        Object opts = map__18963 = object2;
        Object object10 = map__18963;
        map__18963 = null;
        Object as = RT.get((Object)object10, (Object)const__7);
        Object object11 = attr;
        attr = null;
        Object object12 = opts;
        opts = null;
        Object object13 = ((IFn)const__8.getRawRoot()).invoke(object12);
        Object object14 = args;
        args = null;
        Object object15 = ((IFn)const__9.getRawRoot()).invoke(object14);
        Object object16 = as;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = as;
            as = null;
            object = ((IFn)const__10.getRawRoot()).invoke(object17);
        } else {
            object = const__11.getRawRoot();
        }
        return Tuple.create((Object)object11, (Object)object13, (Object)object15, (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$attr_with_opts__GT_attr_tuple.invokeStatic(object2);
    }
}

