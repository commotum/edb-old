/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentSet
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.excise$component_es_set$fn__14771;

public final class excise$component_es_set
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"component-es-set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__6 = RT.keyword(null, (String)"eavt");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__8 = RT.var((String)"clojure.set", (String)"difference");

    public static Object invokeStatic(Object db2, Object e, Object via_attrs) {
        IPersistentSet es;
        IPersistentVector G__14764;
        IPersistentVector vec__14765;
        IPersistentSet es2 = RT.set((Object[])new Object[]{e});
        Object object = e;
        e = null;
        IPersistentVector iPersistentVector = vec__14765 = (G__14764 = Tuple.create((Object)object));
        vec__14765 = null;
        Object seq__14766 = ((IFn)const__1.getRawRoot()).invoke((Object)iPersistentVector);
        Object first__14767 = ((IFn)const__2.getRawRoot()).invoke(seq__14766);
        Object object2 = seq__14766;
        seq__14766 = null;
        Object seq__147662 = ((IFn)const__3.getRawRoot()).invoke(object2);
        first__14767 = null;
        seq__147662 = null;
        Object object3 = via_attrs;
        via_attrs = null;
        Object via = object3;
        IPersistentSet iPersistentSet = es2;
        es2 = null;
        Object es3 = iPersistentSet;
        IPersistentVector iPersistentVector2 = G__14764;
        G__14764 = null;
        Object G__147642 = iPersistentVector2;
        Object object4 = via;
        via = null;
        Object via2 = object4;
        while (true) {
            IPersistentVector vec__14768;
            IPersistentSet iPersistentSet2 = es3;
            es3 = null;
            es = iPersistentSet2;
            IPersistentVector iPersistentVector3 = G__147642;
            G__147642 = null;
            IPersistentVector iPersistentVector4 = vec__14768 = iPersistentVector3;
            vec__14768 = null;
            Object seq__14769 = ((IFn)const__1.getRawRoot()).invoke((Object)iPersistentVector4);
            Object first__14770 = ((IFn)const__2.getRawRoot()).invoke(seq__14769);
            Object object5 = seq__14769;
            seq__14769 = null;
            Object seq__147692 = ((IFn)const__3.getRawRoot()).invoke(object5);
            Object object6 = first__14770;
            first__14770 = null;
            Object check = object6;
            Object object7 = seq__147692;
            seq__147692 = null;
            Object more = object7;
            Object object8 = via2;
            via2 = null;
            Object via3 = object8;
            Object object9 = check;
            if (object9 == null || object9 == Boolean.FALSE) break;
            Object object10 = via3;
            via3 = null;
            Object object11 = check;
            check = null;
            Object comps = ((IFn)const__4.getRawRoot()).invoke((Object)new excise$component_es_set$fn__14771(db2, object10), (Object)PersistentHashSet.EMPTY, ((IFn)const__5.getRawRoot()).invoke(db2, (Object)const__6, (Object)Tuple.create((Object)object11)));
            Object object12 = ((IFn)const__7.getRawRoot()).invoke((Object)es, comps);
            Object object13 = more;
            more = null;
            Object object14 = comps;
            comps = null;
            IPersistentSet iPersistentSet3 = es;
            es = null;
            via2 = null;
            G__147642 = ((IFn)const__7.getRawRoot()).invoke(object13, ((IFn)const__8.getRawRoot()).invoke(object14, (Object)iPersistentSet3));
            es3 = object12;
        }
        IPersistentSet iPersistentSet4 = es;
        es = null;
        return iPersistentSet4;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return excise$component_es_set.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object e) {
        Object object = db2;
        db2 = null;
        Object object2 = e;
        e = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$component_es_set.invokeStatic(object3, object4);
    }
}

