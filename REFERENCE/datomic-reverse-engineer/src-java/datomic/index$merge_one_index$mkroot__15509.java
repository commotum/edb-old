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

public final class index$merge_one_index$mkroot__15509
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__5 = RT.var((String)"datomic.index", (String)"root-node");

    public Object invoke(Object rootnode_data) {
        Object keydata = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), rootnode_data));
        Object object = rootnode_data;
        rootnode_data = null;
        Object dirids = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__4.getRawRoot(), object));
        Object object2 = keydata;
        keydata = null;
        Object object3 = dirids;
        dirids = null;
        index$merge_one_index$mkroot__15509 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(object2, object3);
    }
}

