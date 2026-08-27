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
import java.util.List;

public final class datalog$add_rule$rcnt__18738
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__7 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), Symbol.intern(null, (String)"symbol?"), Symbol.intern(null, (String)"xs")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 24}));
    public static final Object const__11 = 0L;

    public Object invoke(Object p__18737) {
        Object object;
        Object xs;
        Object vec__18739;
        Object object2 = p__18737;
        p__18737 = null;
        Object object3 = vec__18739 = object2;
        vec__18739 = null;
        Object seq__18740 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object first__18741 = ((IFn)const__1.getRawRoot()).invoke(seq__18740);
        Object object4 = seq__18740;
        seq__18740 = null;
        Object seq__187402 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object5 = first__18741;
        first__18741 = null;
        Object p = object5;
        Object first__187412 = ((IFn)const__1.getRawRoot()).invoke(seq__187402);
        Object object6 = seq__187402;
        seq__187402 = null;
        Object seq__187403 = ((IFn)const__2.getRawRoot()).invoke(object6);
        Object object7 = first__187412;
        first__187412 = null;
        Object r = object7;
        Object object8 = seq__187403;
        seq__187403 = null;
        Object object9 = xs = object8;
        xs = null;
        Object object10 = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), object9);
        if (object10 == null || object10 == Boolean.FALSE) {
            Object object11 = p;
            p = null;
            throw (Throwable)((Object)new AssertionError(((IFn)const__5.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke((Object)"Required args list must be first in predicate: ", object11), (Object)"\n", ((IFn)const__6.getRawRoot()).invoke(const__7))));
        }
        if (r instanceof List) {
            Object object12 = r;
            r = null;
            datalog$add_rule$rcnt__18738 this_ = null;
            object = RT.count((Object)object12);
        } else {
            object = const__11;
        }
        return object;
    }
}

