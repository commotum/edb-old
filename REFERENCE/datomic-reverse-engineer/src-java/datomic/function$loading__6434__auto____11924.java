/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
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
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class function$loading__6434__auto____11924
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__2 = RT.keyword(null, (String)"exclude");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"compare")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 28}));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"datomic.janino");
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"compare")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 47})));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.memory-size"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"size"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2, const__3);
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn0"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn1"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn2"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn3"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn4"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn5"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn6"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn7"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn8"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn9"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.functions.Fn10"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

