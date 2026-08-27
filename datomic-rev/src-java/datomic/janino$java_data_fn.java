/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.codehaus.commons.compiler.jdk.ClassBodyEvaluator
 *  org.codehaus.commons.compiler.jdk.ScriptEvaluator
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.codehaus.commons.compiler.jdk.ClassBodyEvaluator;
import org.codehaus.commons.compiler.jdk.ScriptEvaluator;

public final class janino$java_data_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");
    public static final AFn const__1 = (AFn)Tuple.create((Object)"static datomic.Util.*", (Object)"static datomic.Peer.*", (Object)"datomic.functions.*", (Object)"datomic.Database", (Object)"datomic.Datom", (Object)"static datomic.Database.*");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Object const__4 = RT.classForName((String)"java.lang.String");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Keyword const__8 = RT.keyword(null, (String)"tag");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"eval");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)".invoke");

    public static Object invokeStatic(Object params, Object body) {
        Object closer;
        ScriptEvaluator G__11930 = new ScriptEvaluator();
        ((ClassBodyEvaluator)G__11930).setDefaultImports((String[])((IFn)const__0.getRawRoot()).invoke((Object)const__1));
        ScriptEvaluator se = null;
        se = G__11930;
        Object iname = ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.functions.Fn", (Object)RT.count((Object)params));
        Class<?> iface = Class.forName((String)iname);
        ScriptEvaluator scriptEvaluator = se;
        se = null;
        Object object = body;
        body = null;
        Class<?> clazz = iface;
        iface = null;
        Object proc = scriptEvaluator.createFastEvaluator((String)object, clazz, (String[])((IFn)const__0.getRawRoot()).invoke(const__4, ((IFn)const__5.getRawRoot()).invoke(const__2.getRawRoot(), params)));
        Object[] objectArray = new Object[2];
        objectArray[0] = const__8;
        Object object2 = iname;
        iname = null;
        objectArray[1] = ((IFn)const__9.getRawRoot()).invoke(object2);
        Object gproc = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(), (Object)RT.mapUniqueKeys((Object[])objectArray));
        Object object3 = params;
        params = null;
        Object params2 = ((IFn)const__10.getRawRoot()).invoke(const__9.getRawRoot(), object3);
        Object object4 = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot(), ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(gproc)))));
        Object object5 = ((IFn)const__14.getRawRoot()).invoke(params2);
        Object object6 = gproc;
        gproc = null;
        Object object7 = params2;
        params2 = null;
        Object object8 = closer = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke((Object)const__15), object4, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke((Object)const__18), object5, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke((Object)const__19), ((IFn)const__14.getRawRoot()).invoke(object6), object7)))))))));
        closer = null;
        Object object9 = proc;
        proc = null;
        return ((IFn)object8).invoke(object9);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return janino$java_data_fn.invokeStatic(object3, object4);
    }
}

