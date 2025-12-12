/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.TreeIter$fn__15117$fn__15118;

public final class TreeIter$fn__15117
extends AFunction {
    Object lookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"getx-uncached");

    public TreeIter$fn__15117(Object object) {
        this.lookup = object;
    }

    public Object invoke(Object segid) {
        TreeIter$fn__15117$fn__15118 treeIter$fn__15117$fn__15118 = new TreeIter$fn__15117$fn__15118(segid);
        Object object = segid;
        segid = null;
        TreeIter$fn__15117 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)treeIter$fn__15117$fn__15118, ((IFn)const__1.getRawRoot()).invoke(this_.lookup, object));
    }
}

