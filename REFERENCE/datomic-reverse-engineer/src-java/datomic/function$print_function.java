/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.io.Writer;

public final class function$print_function
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"lang"), (Object)RT.keyword(null, (String)"imports"), (Object)RT.keyword(null, (String)"requires"), (Object)RT.keyword(null, (String)"params"), (Object)RT.keyword(null, (String)"code"));

    public static Object invokeStatic(Object dbfn, Object w) {
        Object object = w;
        w = null;
        Object object2 = dbfn;
        dbfn = null;
        ((Writer)object).write((String)((IFn)const__0.getRawRoot()).invoke((Object)"#db/fn", ((IFn)const__1.getRawRoot()).invoke(object2, (Object)const__7)));
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return function$print_function.invokeStatic(object3, object4);
    }
}

