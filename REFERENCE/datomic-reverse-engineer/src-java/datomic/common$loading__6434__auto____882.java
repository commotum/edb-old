/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
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
import clojure.lang.Keyword;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class common$loading__6434__auto____882
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__2 = RT.keyword(null, (String)"exclude");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"compare"), (Object)Symbol.intern(null, (String)"qualified-symbol?"));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"use");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"clojure.pprint");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.java.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.thread"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"thread"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.monitor"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"monitor"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.string"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"str"));
    public static final AFn const__12 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.set"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"set"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2, (Object)const__3);
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5);
            ((IFn)const__6.getRawRoot()).invoke((Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.io.IOException"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.io.InterruptedIOException"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.ExecutorService"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Comparator"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Properties"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.List"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Map"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Set"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Collection"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.ArrayList"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Collections"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Comparator"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Map$Entry"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

