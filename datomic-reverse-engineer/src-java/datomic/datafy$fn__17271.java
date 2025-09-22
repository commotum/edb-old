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

public final class datafy$fn__17271
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"true?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"false?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"or"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"true?"), Symbol.intern(null, (String)"n")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"false?"), Symbol.intern(null, (String)"n")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));

    public static Object invokeStatic(Object n, Object _) {
        Object object;
        Object or__5238__auto__17273;
        Object object2 = or__5238__auto__17273 = ((IFn)const__0.getRawRoot()).invoke(n);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__17273;
            or__5238__auto__17273 = null;
        } else {
            object = ((IFn)const__1.getRawRoot()).invoke(n);
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        }
        Object object3 = n;
        n = null;
        return new Boolean(RT.booleanCast((Object)object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datafy$fn__17271.invokeStatic(object3, object4);
    }
}

