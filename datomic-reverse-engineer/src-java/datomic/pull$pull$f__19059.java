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

public final class pull$pull$f__19059
extends AFunction {
    Object selector;
    Object db;
    Object es;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.pull", (String)"pull*");
    public static final Var const__4 = RT.var((String)"datomic.pull", (String)"normalized-pattern-cache");

    public pull$pull$f__19059(Object object, Object object2, Object object3) {
        this.selector = object;
        this.db = object2;
        this.es = object3;
    }

    public Object invoke() {
        pull$pull$f__19059 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), this_.db, RT.get((Object)const__4.getRawRoot(), (Object)this_.selector), (Object)PersistentHashSet.EMPTY, (Object)Boolean.TRUE), this_.es);
    }
}

