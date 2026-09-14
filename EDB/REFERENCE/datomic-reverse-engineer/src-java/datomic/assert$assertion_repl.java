/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.assert$assertion_repl$fn__20656;
import datomic.assert$assertion_repl$prompt__20654;
import datomic.assert$assertion_repl$stashing_eval__20652;

public final class assert$assertion_repl
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"datomic.assert", (String)"*level*");
    public static final Var const__5 = RT.var((String)"datomic.assert", (String)"*result*");
    public static final Var const__6 = RT.var((String)"clojure.main", (String)"repl");
    public static final Keyword const__7 = RT.keyword(null, (String)"prompt");
    public static final Keyword const__8 = RT.keyword(null, (String)"eval");
    public static final Keyword const__9 = RT.keyword(null, (String)"init");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object error2) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke((Object)"Assertion failed, entering subrepl. See *e for details.\nWhen you close the input stream, the last REPL value will become the\nresult of the assertion expression (and be thrown if a Throwable).");
        ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)Numbers.inc((Object)const__3.get()), (Object)const__5, null));
        try {
            assert$assertion_repl$prompt__20654 prompt;
            assert$assertion_repl$stashing_eval__20652 stashing_eval = new assert$assertion_repl$stashing_eval__20652();
            assert$assertion_repl$prompt__20654 assert$assertion_repl$prompt__20654 = prompt = new assert$assertion_repl$prompt__20654();
            prompt = null;
            assert$assertion_repl$stashing_eval__20652 assert$assertion_repl$stashing_eval__20652 = stashing_eval;
            stashing_eval = null;
            Object object2 = error2;
            error2 = null;
            ((IFn)const__6.getRawRoot()).invoke((Object)const__7, (Object)assert$assertion_repl$prompt__20654, (Object)const__8, (Object)assert$assertion_repl$stashing_eval__20652, (Object)const__9, (Object)new assert$assertion_repl$fn__20656(object2));
            if (const__5.get() instanceof Throwable) {
                throw (Throwable)const__5.get();
            }
            object = const__5.get();
        }
        finally {
            ((IFn)const__12.getRawRoot()).invoke();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return assert$assertion_repl.invokeStatic(object2);
    }
}

