/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class tools$reset_index_ref
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"string?"), Symbol.intern(null, (String)"root-id")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"datomic.cluster", (String)"reset-ref");
    public static final Var const__6 = RT.var((String)"datomic.index", (String)"index-ref-key-name");

    public static Object invokeStatic(Object cluster2, Object root_id2) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(root_id2);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        Object object2 = cluster2;
        Object object3 = cluster2;
        cluster2 = null;
        Object object4 = root_id2;
        root_id2 = null;
        return ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object2, ((IFn)const__6.getRawRoot()).invoke(object3), object4));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$reset_index_ref.invokeStatic(object3, object4);
    }
}

