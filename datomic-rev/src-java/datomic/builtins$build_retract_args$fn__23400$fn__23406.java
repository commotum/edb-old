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

public final class builtins$build_retract_args$fn__23400$fn__23406
extends AFunction {
    Object retract;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj!");

    public builtins$build_retract_args$fn__23400$fn__23406(Object object) {
        this.retract = object;
    }

    public Object invoke(Object result2, Object p__23405) {
        Object object;
        Object object2 = p__23405;
        p__23405 = null;
        Object map__23407 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__23407);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__23407;
            map__23407 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__23407;
            map__23407 = null;
        }
        Object map__234072 = object;
        Object e = RT.get((Object)map__234072, (Object)const__3);
        Object a = RT.get((Object)map__234072, (Object)const__4);
        Object object5 = map__234072;
        map__234072 = null;
        Object v = RT.get((Object)object5, (Object)const__5);
        Object object6 = result2;
        result2 = null;
        Object object7 = e;
        e = null;
        Object object8 = a;
        a = null;
        Object object9 = v;
        v = null;
        builtins$build_retract_args$fn__23400$fn__23406 this_ = null;
        return ((IFn)const__6.getRawRoot()).invoke(object6, (Object)Tuple.create((Object)this_.retract, (Object)object7, (Object)object8, (Object)object9));
    }
}

