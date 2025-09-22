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

public final class pull$index_pull$fn__19038$fn__19039
extends AFunction {
    Object datom;
    Object db;
    Object pull_kw;
    Object cached_selector;
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"pull*");

    public pull$index_pull$fn__19038$fn__19039(Object object, Object object2, Object object3, Object object4) {
        this.datom = object;
        this.db = object2;
        this.pull_kw = object3;
        this.cached_selector = object4;
    }

    public Object invoke() {
        this_.datom = null;
        pull$index_pull$fn__19038$fn__19039 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.cached_selector, (Object)PersistentHashSet.EMPTY, (Object)Boolean.TRUE, ((IFn)this_.pull_kw).invoke(this_.datom));
    }
}

