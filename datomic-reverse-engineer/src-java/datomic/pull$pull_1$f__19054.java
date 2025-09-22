/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class pull$pull_1$f__19054
extends AFunction {
    Object db;
    Object selector;
    Object e;
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"pull*");
    public static final Var const__2 = RT.var((String)"datomic.pull", (String)"normalized-pattern-cache");

    public pull$pull_1$f__19054(Object object, Object object2, Object object3) {
        this.db = object;
        this.selector = object2;
        this.e = object3;
    }

    public Object invoke() {
        pull$pull_1$f__19054 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, RT.get((Object)const__2.getRawRoot(), (Object)this_.selector), (Object)PersistentHashSet.EMPTY, (Object)Boolean.FALSE, this_.e);
    }
}

