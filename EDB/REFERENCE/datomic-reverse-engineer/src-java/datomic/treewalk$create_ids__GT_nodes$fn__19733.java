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
import datomic.treewalk$create_ids__GT_nodes$fn__19733$fn__19734;

public final class treewalk$create_ids__GT_nodes$fn__19733
extends AFunction {
    Object allow_missing_QMARK_;
    Object lookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");

    public treewalk$create_ids__GT_nodes$fn__19733(Object object, Object object2) {
        this.allow_missing_QMARK_ = object;
        this.lookup = object2;
    }

    public Object invoke(Object ids) {
        Object object = ids;
        ids = null;
        treewalk$create_ids__GT_nodes$fn__19733 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke((Object)new treewalk$create_ids__GT_nodes$fn__19733$fn__19734(this_.allow_missing_QMARK_, this_.lookup), object));
    }
}

