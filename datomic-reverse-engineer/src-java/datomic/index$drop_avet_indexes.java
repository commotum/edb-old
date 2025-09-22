/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$drop_avet_indexes$fn__15621;
import java.util.Arrays;

public final class index$drop_avet_indexes
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__6 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"old-root-ids")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 14})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), Symbol.intern(null, (String)"%")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 42}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 35}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));

    public static Object invokeStatic(Object store, Object olookup, Object old_root_ids, Object attrids, Object as_of_t2, Object garbage2) {
        Object object = olookup;
        olookup = null;
        Object object2 = attrids;
        attrids = null;
        Object object3 = as_of_t2;
        as_of_t2 = null;
        Object object4 = store;
        store = null;
        Object object5 = garbage2;
        garbage2 = null;
        Object _PERCENT_ = ((IFn)const__0.getRawRoot()).invoke((Object)new index$drop_avet_indexes$fn__15621(object, object2, object3, object4), (Object)Tuple.create((Object)PersistentVector.EMPTY, (Object)object5), old_root_ids);
        Object object6 = old_root_ids;
        old_root_ids = null;
        if ((long)RT.count((Object)object6) != (long)RT.count((Object)((IFn)const__3.getRawRoot()).invoke(_PERCENT_))) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__4.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke(const__6))));
        }
        Object object7 = _PERCENT_;
        _PERCENT_ = null;
        return object7;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return index$drop_avet_indexes.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

