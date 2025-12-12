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
import datomic.treewalk.Node;

public final class treewalk$create_parent_node
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"create-parent-node");
    public static final Var const__1 = RT.var((String)"datomic.treewalk", (String)"create-ids->nodes");
    public static final Var const__2 = RT.var((String)"datomic.treewalk", (String)"lookup-val");

    public static Object invokeStatic(Object id, Object walker, Object lookup, Object allow_missing_QMARK_) {
        Node node;
        Object temp__5457__auto__19739;
        ((IFn)const__1.getRawRoot()).invoke(lookup, allow_missing_QMARK_);
        Object object = lookup;
        lookup = null;
        Object object2 = allow_missing_QMARK_;
        allow_missing_QMARK_ = null;
        Object object3 = temp__5457__auto__19739 = ((IFn)const__2.getRawRoot()).invoke(object, id, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__19739;
            temp__5457__auto__19739 = null;
            Object top = object4;
            id = null;
            walker = null;
            top = null;
            node = new Node(id, ((IFn)walker).invoke(top));
        } else {
            node = null;
        }
        return node;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return treewalk$create_parent_node.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object id, Object walker, Object lookup) {
        Object object = id;
        id = null;
        Object object2 = walker;
        walker = null;
        Object object3 = lookup;
        lookup = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return treewalk$create_parent_node.invokeStatic(object4, object5, object6);
    }
}

