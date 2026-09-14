/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.log.LogDir;
import java.util.Arrays;

public final class log$fressianed_dir
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__6 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"instance?"), Symbol.intern(null, (String)"LogDir"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), Symbol.intern(null, (String)"val")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 31}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
    public static final Var const__7 = RT.var((String)"datomic.fressian", (String)"byte-buf");
    public static final Keyword const__8 = RT.keyword(null, (String)"handlers");
    public static final Var const__9 = RT.var((String)"datomic.log", (String)"write-handlers");
    public static final Keyword const__10 = RT.keyword(null, (String)"footer");

    public static Object invokeStatic(Object val) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(val);
        if (object != null && object != Boolean.FALSE) {
            if (((IFn)const__3.getRawRoot()).invoke(val) instanceof LogDir) {
            } else {
                throw (Throwable)((Object)new AssertionError(((IFn)const__4.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke(const__6))));
            }
        }
        Object object2 = val;
        val = null;
        return ((IFn)const__7.getRawRoot()).invoke(object2, (Object)const__8, const__9.getRawRoot(), (Object)const__10, (Object)Boolean.TRUE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fressianed_dir.invokeStatic(object2);
    }
}

