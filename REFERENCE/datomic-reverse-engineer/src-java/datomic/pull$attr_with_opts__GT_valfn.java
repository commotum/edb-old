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
import datomic.pull$attr_with_opts__GT_valfn$fn__18956;

public final class pull$attr_with_opts__GT_valfn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Keyword const__5 = RT.keyword(null, (String)"default");
    public static final Keyword const__6 = RT.keyword(null, (String)"xform");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__10 = RT.var((String)"datomic.pull", (String)"try-xform");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"contains?");

    public static Object invokeStatic(Object p__18950) {
        Object object;
        Object object2;
        Object map__18954;
        Object object3;
        Object args;
        Object seq__18952;
        Object vec__18951;
        Object object4 = p__18950;
        p__18950 = null;
        Object object5 = vec__18951 = object4;
        vec__18951 = null;
        Object object6 = seq__18952 = ((IFn)const__0.getRawRoot()).invoke(object5);
        seq__18952 = null;
        Object object7 = args = object6;
        args = null;
        Object map__189542 = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object7);
        Object object8 = ((IFn)const__3.getRawRoot()).invoke(map__189542);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__189542;
            map__189542 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__0.getRawRoot()).invoke(object9)));
        } else {
            object3 = map__189542;
            map__189542 = null;
        }
        Object opts = map__18954 = object3;
        Object object10 = RT.get((Object)map__18954, (Object)const__5);
        Object object11 = map__18954;
        map__18954 = null;
        Object xform = RT.get((Object)object11, (Object)const__6);
        IFn iFn = (IFn)const__1.getRawRoot();
        Object object12 = const__7.getRawRoot();
        IPersistentVector G__18955 = Tuple.create((Object)const__8.getRawRoot());
        Object object13 = xform;
        if (object13 != null && object13 != Boolean.FALSE) {
            Object object14 = xform;
            xform = null;
            IPersistentVector iPersistentVector = G__18955;
            G__18955 = null;
            object2 = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object14), (Object)iPersistentVector);
        } else {
            object2 = G__18955;
            G__18955 = null;
        }
        IPersistentVector G__189552 = object2;
        Object object15 = opts;
        opts = null;
        Object object16 = ((IFn)const__11.getRawRoot()).invoke(object15, (Object)const__5);
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = object10;
            object10 = null;
            IPersistentVector iPersistentVector = G__189552;
            G__189552 = null;
            object = ((IFn)const__9.getRawRoot()).invoke((Object)new pull$attr_with_opts__GT_valfn$fn__18956(object17), (Object)iPersistentVector);
        } else {
            object = G__189552;
            G__189552 = null;
        }
        return iFn.invoke(object12, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$attr_with_opts__GT_valfn.invokeStatic(object2);
    }
}

