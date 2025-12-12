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
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class datafy$loading__6434__auto____17154
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.data"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"data"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.reflect"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"reflect"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.string"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"str"));
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"use");
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.pprint"), (Object)RT.keyword(null, (String)"only"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"pprint")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 31})));

    public Object invoke() {
        Object object;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.beans.Introspector"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.beans.PropertyDescriptor"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.lang.reflect.Method"));
            object = ((IFn)const__6.getRawRoot()).invoke((Object)const__7);
        }
        finally {
            Var.popThreadBindings();
        }
        return object;
    }
}

