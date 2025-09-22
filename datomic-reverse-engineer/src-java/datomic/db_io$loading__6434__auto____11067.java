/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.Namespace
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
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db_io$loading__6434__auto____11067
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.coordination"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"coord"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.domain"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"domain"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"index"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.log"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"log"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.uri"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"uri"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9);
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Database"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

