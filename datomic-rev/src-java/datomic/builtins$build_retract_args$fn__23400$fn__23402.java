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

public final class builtins$build_retract_args$fn__23400$fn__23402
extends AFunction {
    Object retract;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Var const__6 = RT.var((String)"datomic.builtins", (String)"component-attr?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"conj!");

    public builtins$build_retract_args$fn__23400$fn__23402(Object object, Object object2) {
        this.retract = object;
        this.db = object2;
    }

    public Object invoke(Object result2, Object p__23401) {
        Object object;
        Object object2;
        Object object3 = p__23401;
        p__23401 = null;
        Object map__23403 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__23403);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__23403;
            map__23403 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__23403;
            map__23403 = null;
        }
        Object map__234032 = object2;
        Object e = RT.get((Object)map__234032, (Object)const__3);
        Object a = RT.get((Object)map__234032, (Object)const__4);
        Object object6 = map__234032;
        map__234032 = null;
        Object v = RT.get((Object)object6, (Object)const__5);
        Object object7 = ((IFn)const__6.getRawRoot()).invoke(this_.db, a);
        if (object7 != null && object7 != Boolean.FALSE) {
            object = result2;
            result2 = null;
        } else {
            Object object8 = result2;
            result2 = null;
            Object object9 = e;
            e = null;
            Object object10 = a;
            a = null;
            Object object11 = v;
            v = null;
            builtins$build_retract_args$fn__23400$fn__23402 this_ = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object8, (Object)Tuple.create((Object)this_.retract, (Object)object9, (Object)object10, (Object)object11));
        }
        return object;
    }
}

