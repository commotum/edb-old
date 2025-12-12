/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Associative;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class qtune$loading__6434__auto____23415
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"d"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.query"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"query"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.datalog"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"datalog"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.edn"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"edn"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.set"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"set"));

    public Object invoke() {
        Object object;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8);
        }
        finally {
            Var.popThreadBindings();
        }
        return object;
    }
}

