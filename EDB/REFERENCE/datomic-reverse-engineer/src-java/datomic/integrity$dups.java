/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class integrity$dups
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"distinct");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"sort");
    public static final Var const__3 = RT.var((String)"datomic.api", (String)"q");
    public static final AFn const__4 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"find"), Symbol.intern(null, (String)"?e"), Symbol.intern(null, (String)"?e2"), RT.keyword(null, (String)"in"), Symbol.intern(null, (String)"$"), Symbol.intern(null, (String)"?attr"), RT.keyword(null, (String)"where"), Tuple.create((Object)Symbol.intern(null, (String)"?e"), (Object)Symbol.intern(null, (String)"?attr"), (Object)Symbol.intern(null, (String)"?v")), Tuple.create((Object)Symbol.intern(null, (String)"?e2"), (Object)Symbol.intern(null, (String)"?attr"), (Object)Symbol.intern(null, (String)"?v")), Tuple.create((Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"!="), Symbol.intern(null, (String)"?e"), Symbol.intern(null, (String)"?e2")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 26})))});

    public static Object invokeStatic(Object db2, Object attr) {
        Object object = db2;
        db2 = null;
        Object object2 = attr;
        attr = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke((Object)const__4, object, object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$dups.invokeStatic(object3, object4);
    }
}

