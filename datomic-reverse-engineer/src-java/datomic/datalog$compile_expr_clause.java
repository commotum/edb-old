/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$compile_expr_clause$iter__18617__18621;

public final class datalog$compile_expr_clause
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"clojure.core", (String)"mapv");
    public static final AFn const__9 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"t__18615__auto__");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"clojure.core", (String)"object-array");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"range");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"clojure.core", (String)"dotimes");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"i__18616__auto__");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"clojure.core", (String)"aset");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"clojure.core", (String)"+");
    public static final AFn const__20 = (AFn)Symbol.intern((String)"clojure.core", (String)"nth");
    public static final AFn const__21 = (AFn)Symbol.intern((String)"datomic.datalog", (String)"tuple");
    public static final Keyword const__22 = RT.keyword(null, (String)"scalar");
    public static final AFn const__23 = (AFn)Symbol.intern((String)"datomic.datalog", (String)"scalar->rel");
    public static final Keyword const__24 = RT.keyword(null, (String)"tuple");
    public static final AFn const__25 = (AFn)Symbol.intern((String)"datomic.datalog", (String)"tuple->rel");
    public static final Keyword const__26 = RT.keyword(null, (String)"list");
    public static final AFn const__27 = (AFn)Symbol.intern((String)"clojure.core", (String)"mapv");
    public static final AFn const__28 = (AFn)Symbol.intern((String)"clojure.core", (String)"vector");
    public static final Keyword const__29 = RT.keyword(null, (String)"rel");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"str");
    public static final AFn const__31 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"*ns*");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"find-ns");
    public static final AFn const__36 = (AFn)Symbol.intern(null, (String)"datomic.extensions");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
    public static final Var const__38 = RT.var((String)"clojure.core", (String)"eval");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Object invokeStatic(Object sources, Object vars, Object expr, Object bind_type, Object binds) {
        Object object;
        block11: {
            Object params;
            block9: {
                Object object2;
                IFn iFn;
                Object object3;
                Object object4;
                IFn iFn2;
                IFn iFn3;
                IFn iFn4;
                Object object5;
                Object object6;
                IFn iFn5;
                IFn iFn6;
                block10: {
                    datalog$compile_expr_clause$iter__18617__18621 iter__6025__auto__18632;
                    Object object7 = sources;
                    sources = null;
                    params = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object7, vars));
                    long retlen = (long)RT.count((Object)vars) + (long)RT.count((Object)binds);
                    Object gret = ((IFn)const__4.getRawRoot()).invoke();
                    Object object8 = bind_type;
                    if (object8 == null || object8 == Boolean.FALSE) break block9;
                    iFn6 = (IFn)const__5.getRawRoot();
                    iFn5 = (IFn)const__1.getRawRoot();
                    object6 = ((IFn)const__6.getRawRoot()).invoke((Object)const__7);
                    Object object9 = params;
                    params = null;
                    object5 = ((IFn)const__6.getRawRoot()).invoke(object9);
                    iFn4 = (IFn)const__6.getRawRoot();
                    iFn3 = (IFn)const__5.getRawRoot();
                    iFn2 = (IFn)const__1.getRawRoot();
                    object4 = ((IFn)const__6.getRawRoot()).invoke((Object)const__8);
                    Object object10 = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(gret), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__14), ((IFn)const__6.getRawRoot()).invoke((Object)Numbers.num((long)retlen)))))))));
                    datalog$compile_expr_clause$iter__18617__18621 datalog$compile_expr_clause$iter__18617__18621 = iter__6025__auto__18632 = new datalog$compile_expr_clause$iter__18617__18621(gret, vars);
                    iter__6025__auto__18632 = null;
                    Object object11 = ((IFn)datalog$compile_expr_clause$iter__18617__18621).invoke(((IFn)const__15.getRawRoot()).invoke((Object)RT.count((Object)vars)));
                    Object object12 = binds;
                    binds = null;
                    Object object13 = vars;
                    vars = null;
                    Object object14 = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__16), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__17), ((IFn)const__6.getRawRoot()).invoke((Object)RT.count((Object)object12)))))), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__18), ((IFn)const__6.getRawRoot()).invoke(gret), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__19), ((IFn)const__6.getRawRoot()).invoke((Object)const__17), ((IFn)const__6.getRawRoot()).invoke((Object)RT.count((Object)object13))))), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__20), ((IFn)const__6.getRawRoot()).invoke((Object)const__12), ((IFn)const__6.getRawRoot()).invoke((Object)const__17))))))))));
                    Object object15 = gret;
                    gret = null;
                    object3 = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__9), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__12))))), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__13), object10, object11, object14, ((IFn)const__6.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__21), ((IFn)const__6.getRawRoot()).invoke(object15))))))))));
                    iFn = (IFn)const__6.getRawRoot();
                    Object object16 = bind_type;
                    bind_type = null;
                    Object G__18630 = object16;
                    switch (Util.hash((Object)G__18630) >> 16 & 3) {
                        case 0: {
                            if (G__18630 != const__22) break;
                            Object object17 = expr;
                            expr = null;
                            object2 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__23), ((IFn)const__6.getRawRoot()).invoke(object17)));
                            break block10;
                        }
                        case 1: {
                            if (G__18630 != const__24) break;
                            Object object18 = expr;
                            expr = null;
                            object2 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__25), ((IFn)const__6.getRawRoot()).invoke(object18)));
                            break block10;
                        }
                        case 2: {
                            if (G__18630 != const__26) break;
                            Object object19 = expr;
                            expr = null;
                            object2 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__27), ((IFn)const__6.getRawRoot()).invoke((Object)const__28), ((IFn)const__6.getRawRoot()).invoke(object19)));
                            break block10;
                        }
                        case 3: {
                            if (G__18630 != const__29) break;
                            object2 = expr;
                            expr = null;
                            break block10;
                        }
                    }
                    Object object20 = G__18630;
                    G__18630 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__30.getRawRoot()).invoke((Object)"No matching clause: ", object20));
                }
                object = iFn6.invoke(iFn5.invoke(object6, object5, iFn4.invoke(iFn3.invoke(iFn2.invoke(object4, object3, iFn.invoke(object2))))));
                break block11;
            }
            Object object21 = params;
            params = null;
            Object object22 = expr;
            expr = null;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__31), ((IFn)const__6.getRawRoot()).invoke(object21), ((IFn)const__6.getRawRoot()).invoke(object22)));
        }
        Object fexpr = object;
        ((IFn)const__32.getRawRoot()).invoke(((IFn)const__33.getRawRoot()).invoke((Object)const__34, ((IFn)const__35.getRawRoot()).invoke((Object)const__36), (Object)const__37, (Object)Boolean.TRUE));
        try {
            Object object23 = fexpr;
            fexpr = null;
            Object object24 = ((IFn)const__38.getRawRoot()).invoke(object23);
            return object24;
        }
        finally {
            ((IFn)const__39.getRawRoot()).invoke();
        }
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return datalog$compile_expr_clause.invokeStatic(object6, object7, object8, object9, object10);
    }
}

