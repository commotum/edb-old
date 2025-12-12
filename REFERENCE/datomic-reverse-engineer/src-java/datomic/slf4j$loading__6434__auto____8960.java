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

public final class slf4j$loading__6434__auto____8960
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.math"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"math"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.edn"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"edn"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.string"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"str"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"org.slf4j.Logger"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"org.slf4j.LoggerFactory"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

