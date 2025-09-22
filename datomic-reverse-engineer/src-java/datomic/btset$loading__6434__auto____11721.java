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

public final class btset$loading__6434__auto____11721
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__2 = RT.keyword(null, (String)"exclude");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"comp"), (Object)Symbol.intern(null, (String)"compare"));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"use");
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"only"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"compare")));
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.iter"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"iter"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"iget"), Symbol.intern(null, (String)"inext"), Symbol.intern(null, (String)"iprev")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 43})));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2, (Object)const__3);
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5);
            ((IFn)const__6.getRawRoot()).invoke((Object)const__7);
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.iter.Iter"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

