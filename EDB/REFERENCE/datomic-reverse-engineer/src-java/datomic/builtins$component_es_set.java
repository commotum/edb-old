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
import datomic.builtins$component_es_set$fn__23395;

public final class builtins$component_es_set
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.builtins", (String)"component-es-set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__6 = RT.keyword(null, (String)"eavt");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__8 = RT.var((String)"clojure.set", (String)"difference");

    public static Object invokeStatic(Object db2, Object e, Object via_attrs) {
        IPersistentSet iPersistentSet;
        Object object = e;
        if (object != null && object != Boolean.FALSE) {
            IPersistentSet es;
            IPersistentVector G__23388;
            IPersistentVector vec__23389;
            IPersistentSet es2 = RT.set((Object[])new Object[]{e});
            Object object2 = e;
            e = null;
            IPersistentVector iPersistentVector = vec__23389 = (G__23388 = Tuple.create((Object)object2));
            vec__23389 = null;
            Object seq__23390 = ((IFn)const__1.getRawRoot()).invoke((Object)iPersistentVector);
            Object first__23391 = ((IFn)const__2.getRawRoot()).invoke(seq__23390);
            Object object3 = seq__23390;
            seq__23390 = null;
            Object seq__233902 = ((IFn)const__3.getRawRoot()).invoke(object3);
            first__23391 = null;
            seq__233902 = null;
            Object object4 = via_attrs;
            via_attrs = null;
            Object via = object4;
            IPersistentSet iPersistentSet2 = es2;
            es2 = null;
            Object es3 = iPersistentSet2;
            IPersistentVector iPersistentVector2 = G__23388;
            G__23388 = null;
            Object G__233882 = iPersistentVector2;
            Object object5 = via;
            via = null;
            Object via2 = object5;
            while (true) {
                IPersistentVector vec__23392;
                IPersistentSet iPersistentSet3 = es3;
                es3 = null;
                es = iPersistentSet3;
                IPersistentVector iPersistentVector3 = G__233882;
                G__233882 = null;
                IPersistentVector iPersistentVector4 = vec__23392 = iPersistentVector3;
                vec__23392 = null;
                Object seq__23393 = ((IFn)const__1.getRawRoot()).invoke((Object)iPersistentVector4);
                Object first__23394 = ((IFn)const__2.getRawRoot()).invoke(seq__23393);
                Object object6 = seq__23393;
                seq__23393 = null;
                Object seq__233932 = ((IFn)const__3.getRawRoot()).invoke(object6);
                Object object7 = first__23394;
                first__23394 = null;
                Object check = object7;
                Object object8 = seq__233932;
                seq__233932 = null;
                Object more = object8;
                Object object9 = via2;
                via2 = null;
                Object via3 = object9;
                Object object10 = check;
                if (object10 == null || object10 == Boolean.FALSE) break;
                Object object11 = via3;
                via3 = null;
                Object object12 = check;
                check = null;
                Object comps = ((IFn)const__4.getRawRoot()).invoke((Object)new builtins$component_es_set$fn__23395(object11, db2), (Object)PersistentHashSet.EMPTY, ((IFn)const__5.getRawRoot()).invoke(db2, (Object)const__6, (Object)Tuple.create((Object)object12)));
                Object object13 = ((IFn)const__7.getRawRoot()).invoke((Object)es, comps);
                Object object14 = more;
                more = null;
                Object object15 = comps;
                comps = null;
                IPersistentSet iPersistentSet4 = es;
                es = null;
                via2 = null;
                G__233882 = ((IFn)const__7.getRawRoot()).invoke(object14, ((IFn)const__8.getRawRoot()).invoke(object15, (Object)iPersistentSet4));
                es3 = object13;
            }
            iPersistentSet = es;
            es = null;
        } else {
            iPersistentSet = null;
        }
        return iPersistentSet;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return builtins$component_es_set.invokeStatic(object4, object5, object6);
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
        return builtins$component_es_set.invokeStatic(object3, object4);
    }
}

