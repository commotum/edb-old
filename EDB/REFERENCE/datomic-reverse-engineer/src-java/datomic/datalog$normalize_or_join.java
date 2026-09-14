/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$normalize_or_join$fn__18711;

public final class datalog$normalize_or_join
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"or-join");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__7 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"or-binding-empty");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"list*");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"meta");

    public static Object invokeStatic(Object p__18707) {
        Object object;
        Object object2 = p__18707;
        p__18707 = null;
        Object vec__18708 = object2;
        Object seq__18709 = ((IFn)const__0.getRawRoot()).invoke(vec__18708);
        Object first__18710 = ((IFn)const__1.getRawRoot()).invoke(seq__18709);
        Object object3 = seq__18709;
        seq__18709 = null;
        Object seq__187092 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__18710;
        first__18710 = null;
        Object p = object4;
        Object first__187102 = ((IFn)const__1.getRawRoot()).invoke(seq__187092);
        Object object5 = seq__187092;
        seq__187092 = null;
        Object seq__187093 = ((IFn)const__2.getRawRoot()).invoke(object5);
        Object object6 = first__187102;
        first__187102 = null;
        Object vs = object6;
        Object object7 = seq__187093;
        seq__187093 = null;
        Object cs = object7;
        Object object8 = vec__18708;
        vec__18708 = null;
        Object c = object8;
        if (Util.equiv((Object)const__4, (Object)p)) {
            Object object9 = cs;
            cs = null;
            Object cs2 = ((IFn)const__5.getRawRoot()).invoke((Object)new datalog$normalize_or_join$fn__18711(), object9);
            Object object10 = ((IFn)const__6.getRawRoot()).invoke(vs);
            if (object10 != null && object10 != Boolean.FALSE) {
                ((IFn)const__7.getRawRoot()).invoke((Object)const__8, ((IFn)const__9.getRawRoot()).invoke((Object)"'or' cannot have empty binding set: ", c));
            }
            Object object11 = p;
            p = null;
            Object object12 = vs;
            vs = null;
            Object object13 = cs2;
            cs2 = null;
            Object object14 = c;
            c = null;
            object = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object11, object12, object13), ((IFn)const__12.getRawRoot()).invoke(object14));
        } else {
            object = c;
            c = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$normalize_or_join.invokeStatic(object2);
    }
}

