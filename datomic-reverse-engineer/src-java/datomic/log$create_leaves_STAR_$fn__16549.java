/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class log$create_leaves_STAR_$fn__16549
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ffirst");
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"unchunk");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__3 = RT.var((String)"datomic.log", (String)"BEGIN_CLOSED_LIST");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"END_COLLECTION");

    public Object invoke(Object ftxes) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(ftxes);
        Object object2 = ftxes;
        ftxes = null;
        return Tuple.create((Object)object, (Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)Tuple.create((Object)const__3.getRawRoot()), ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), object2), (Object)Tuple.create((Object)const__6.getRawRoot()))));
    }
}

