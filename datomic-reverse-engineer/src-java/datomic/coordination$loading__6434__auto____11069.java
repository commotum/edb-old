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

public final class coordination$loading__6434__auto____11069
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cache"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cache"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.catalog"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"catalog"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster-stack"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster-stack"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.codec"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"codec"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.h2"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"h2"));
    public static final AFn const__12 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io"));
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.kv-cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"kvc"));
    public static final AFn const__14 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.kv-sql"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"kvsql"));
    public static final AFn const__15 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));

    public Object invoke() {
        Object object;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12, (Object)const__13, (Object)const__14, (Object)const__15);
        }
        finally {
            Var.popThreadBindings();
        }
        return object;
    }
}

