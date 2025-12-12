/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datalog$or_join__GT_rule_preds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Keyword const__8 = RT.keyword((String)"query-stats", (String)"clause");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"list*");

    public static Object invokeStatic(Object p__18714) {
        Object head;
        Object rname;
        Object vec__18715;
        Object object = p__18714;
        p__18714 = null;
        Object object2 = vec__18715 = object;
        vec__18715 = null;
        Object seq__18716 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object first__18717 = ((IFn)const__1.getRawRoot()).invoke(seq__18716);
        Object object3 = seq__18716;
        seq__18716 = null;
        Object seq__187162 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__18717;
        first__18717 = null;
        Object p = object4;
        Object first__187172 = ((IFn)const__1.getRawRoot()).invoke(seq__187162);
        Object object5 = seq__187162;
        seq__187162 = null;
        Object seq__187163 = ((IFn)const__2.getRawRoot()).invoke(object5);
        Object object6 = first__187172;
        first__187172 = null;
        Object vs = object6;
        Object object7 = seq__187163;
        seq__187163 = null;
        Object cs = object7;
        Object object8 = rname = ((IFn)const__3.getRawRoot()).invoke((Object)"arule__");
        rname = null;
        Object object9 = head = ((IFn)const__4.getRawRoot()).invoke(object8, vs);
        head = null;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__8;
        Object object10 = p;
        p = null;
        Object object11 = vs;
        vs = null;
        objectArray[1] = ((IFn)const__9.getRawRoot()).invoke(object10, (Object)Tuple.create((Object)object11));
        Object object12 = cs;
        cs = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__4.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke(object9, (Object)RT.mapUniqueKeys((Object[])objectArray))), object12);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$or_join__GT_rule_preds.invokeStatic(object2);
    }
}

