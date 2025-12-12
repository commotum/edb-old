/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class treewalk$index_tree_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"index-tree-seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"id");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"lookup");
    public static final Var const__5 = RT.var((String)"datomic.treewalk", (String)"create-ids->nodes");
    public static final Var const__6 = RT.var((String)"datomic.treewalk", (String)"lookup-val");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__8 = RT.var((String)"datomic.treewalk", (String)"tree-node-ids");
    public static final Var const__9 = RT.var((String)"datomic.treewalk", (String)"index-top-walker");

    public static Object invokeStatic(Object id, Object lookup, Object allow_missing_QMARK_) {
        Object object;
        Object temp__5457__auto__19748;
        Object object2 = id;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke((Object)const__3))));
        }
        Object object3 = lookup;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke((Object)const__4))));
        }
        Object ids__GT_nodes = ((IFn)const__5.getRawRoot()).invoke(lookup, allow_missing_QMARK_);
        Object object4 = lookup;
        lookup = null;
        Object object5 = allow_missing_QMARK_;
        allow_missing_QMARK_ = null;
        Object object6 = temp__5457__auto__19748 = ((IFn)const__6.getRawRoot()).invoke(object4, id, object5);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object top;
            Object object7 = temp__5457__auto__19748;
            temp__5457__auto__19748 = null;
            Object object8 = top = object7;
            top = null;
            Object object9 = ids__GT_nodes;
            ids__GT_nodes = null;
            Object object10 = id;
            id = null;
            object = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(object8), object9), (Object)Tuple.create((Object)object10));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return treewalk$index_tree_seq.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object id, Object lookup) {
        Object object = id;
        id = null;
        Object object2 = lookup;
        lookup = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return treewalk$index_tree_seq.invokeStatic(object3, object4);
    }
}

