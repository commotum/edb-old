/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$create_leaves_STAR_$fn__16547;
import datomic.log$create_leaves_STAR_$fn__16549;

public final class log$create_leaves_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"remaining");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"sequence");
    public static final Var const__4 = RT.var((String)"datomic.log", (String)"partition-by-weight");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"combine-last-if");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object target_size, Object ftxes) {
        Object weigh = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), const__2.getRawRoot());
        Object object = ((IFn)const__4.getRawRoot()).invoke(weigh, target_size);
        Object object2 = weigh;
        weigh = null;
        Object object3 = target_size;
        target_size = null;
        Object object4 = ftxes;
        ftxes = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__5.getRawRoot()).invoke((Object)new log$create_leaves_STAR_$fn__16547(object2, object3), const__6.getRawRoot()), ((IFn)const__7.getRawRoot()).invoke((Object)new log$create_leaves_STAR_$fn__16549())), object4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$create_leaves_STAR_.invokeStatic(object3, object4);
    }
}

