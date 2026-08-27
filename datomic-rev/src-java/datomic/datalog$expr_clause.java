/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$expr_clause$fn__18646;
import datomic.datalog$expr_clause$fn__18648;
import datomic.datalog$expr_clause$fn__18650;
import java.util.Arrays;
import java.util.List;

public final class datalog$expr_clause
extends AFunction {
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"list*");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__12 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__13 = RT.var((String)"datomic.datalog", (String)"source?");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"not=");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"ground");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__19 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"or"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"not="), Symbol.intern(null, (String)"f"), PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"quote"), Symbol.intern(null, (String)"ground")))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 25})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"nil?"), Symbol.intern(null, (String)"vars")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 42}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 21}));
    public static final Var const__20 = RT.var((String)"datomic.datalog", (String)"binding-type");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"vec");
    public static final Keyword const__22 = RT.keyword(null, (String)"scalar");
    public static final Keyword const__23 = RT.keyword(null, (String)"tuple");
    public static final Keyword const__24 = RT.keyword(null, (String)"list");
    public static final Keyword const__25 = RT.keyword(null, (String)"rel");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"not");
    public static final AFn const__28 = (AFn)Symbol.intern(null, (String)"ground");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__30 = RT.var((String)"datomic.datalog", (String)"compile-expr-clause");
    public static final Object const__33 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"sources")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 18})), 2L))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15}));
    public static final Object const__34 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"nil?"), Symbol.intern(null, (String)"xtra")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15}));
    public static final Keyword const__35 = RT.keyword(null, (String)"argvars");
    public static final Keyword const__36 = RT.keyword(null, (String)"fn");
    public static final Keyword const__37 = RT.keyword(null, (String)"clause");
    public static final Keyword const__38 = RT.keyword(null, (String)"binds");
    public static final Keyword const__39 = RT.keyword(null, (String)"bind-type");
    public static final Keyword const__40 = RT.keyword(null, (String)"needs-source");
    public static final Var const__41 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Keyword const__42 = RT.keyword(null, (String)"tag");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object p__18637) {
        Object object;
        Object f;
        Object needs_source;
        Object binds;
        Object bind_type;
        Object sources;
        Object vars;
        Object object2;
        Object clause;
        Object xtra;
        block25: {
            Object object3;
            Object expr;
            block24: {
                boolean and__5236__auto__18655;
                Object object4;
                Object f2;
                block23: {
                    Object and__5236__auto__18654;
                    block20: {
                        Object object5;
                        IFn iFn;
                        block22: {
                            Object object6;
                            Object binds2;
                            block21: {
                                block19: {
                                    Object object7;
                                    Object or__5238__auto__18653;
                                    Object object8 = p__18637;
                                    p__18637 = null;
                                    Object vec__18638 = object8;
                                    Object call = RT.nth((Object)vec__18638, (int)RT.uncheckedIntCast((long)0L), null);
                                    binds2 = RT.nth((Object)vec__18638, (int)RT.uncheckedIntCast((long)1L), null);
                                    xtra = RT.nth((Object)vec__18638, (int)RT.uncheckedIntCast((long)2L), null);
                                    Object object9 = vec__18638;
                                    vec__18638 = null;
                                    clause = object9;
                                    if (!(call instanceof List)) {
                                        object2 = clause;
                                        return object2;
                                    }
                                    Object object10 = call;
                                    call = null;
                                    Object vec__18641 = ((IFn)const__6.getRawRoot()).invoke(object10);
                                    Object seq__18642 = ((IFn)const__7.getRawRoot()).invoke(vec__18641);
                                    Object first__18643 = ((IFn)const__8.getRawRoot()).invoke(seq__18642);
                                    Object object11 = seq__18642;
                                    seq__18642 = null;
                                    Object seq__186422 = ((IFn)const__9.getRawRoot()).invoke(object11);
                                    Object object12 = first__18643;
                                    first__18643 = null;
                                    f2 = object12;
                                    Object object13 = seq__186422;
                                    seq__186422 = null;
                                    Object body = object13;
                                    Object object14 = vec__18641;
                                    vec__18641 = null;
                                    expr = object14;
                                    vars = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), body)));
                                    Object object15 = body;
                                    body = null;
                                    sources = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(const__13.getRawRoot(), object15)));
                                    Object object16 = or__5238__auto__18653 = ((IFn)const__14.getRawRoot()).invoke(f2, (Object)const__15);
                                    if (object16 != null && object16 != Boolean.FALSE) {
                                        object7 = or__5238__auto__18653;
                                        or__5238__auto__18653 = null;
                                    } else {
                                        object7 = Util.identical((Object)vars, null) ? Boolean.TRUE : Boolean.FALSE;
                                    }
                                    if (object7 == null || object7 == Boolean.FALSE) break block19;
                                    bind_type = ((IFn)const__20.getRawRoot()).invoke(binds2);
                                    object6 = and__5236__auto__18654 = binds2;
                                    if (object6 == null) break block20;
                                    break block21;
                                }
                                throw (Throwable)((Object)new AssertionError(((IFn)const__17.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"Can't have variable in ground expression", (Object)"\n", ((IFn)const__18.getRawRoot()).invoke(const__19))));
                            }
                            if (object6 == Boolean.FALSE) break block20;
                            iFn = (IFn)const__21.getRawRoot();
                            Object G__18644 = bind_type;
                            switch (Util.hash((Object)G__18644) >> 16 & 3) {
                                case 0: {
                                    if (G__18644 != const__22) break;
                                    Object object17 = binds2;
                                    binds2 = null;
                                    object5 = Tuple.create((Object)object17);
                                    break block22;
                                }
                                case 1: {
                                    if (G__18644 != const__23) break;
                                    object5 = binds2;
                                    binds2 = null;
                                    break block22;
                                }
                                case 2: {
                                    if (G__18644 != const__24) break;
                                    Object object18 = binds2;
                                    binds2 = null;
                                    object5 = Tuple.create((Object)((IFn)const__8.getRawRoot()).invoke(object18));
                                    break block22;
                                }
                                case 3: {
                                    if (G__18644 != const__25) break;
                                    Object object19 = binds2;
                                    binds2 = null;
                                    object5 = ((IFn)const__8.getRawRoot()).invoke(object19);
                                    break block22;
                                }
                            }
                            Object object20 = G__18644;
                            G__18644 = null;
                            throw (Throwable)new IllegalArgumentException((String)((IFn)const__17.getRawRoot()).invoke((Object)"No matching clause: ", object20));
                        }
                        object4 = iFn.invoke(object5);
                        break block23;
                    }
                    object4 = and__5236__auto__18654;
                    and__5236__auto__18654 = null;
                }
                binds = object4;
                needs_source = ((IFn)const__26.getRawRoot()).invoke((Object)(Util.identical((Object)sources, null) ? Boolean.TRUE : Boolean.FALSE));
                Object object21 = f2;
                f2 = null;
                boolean and__5236__auto__18656 = Util.equiv((Object)object21, (Object)const__28);
                Object object22 = and__5236__auto__18656 ? ((and__5236__auto__18655 = Util.identical((Object)vars, null)) ? needs_source : (and__5236__auto__18655 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__18656 ? Boolean.TRUE : Boolean.FALSE);
                if (object22 == null || object22 == Boolean.FALSE) break block24;
                Object G__18645 = bind_type;
                switch (Util.hash((Object)G__18645) >> 16 & 3) {
                    case 0: {
                        if (G__18645 != const__22) break;
                        object3 = new datalog$expr_clause$fn__18646();
                        break block25;
                    }
                    case 1: {
                        if (G__18645 != const__23) break;
                        object3 = new datalog$expr_clause$fn__18648();
                        break block25;
                    }
                    case 2: {
                        if (G__18645 != const__24) break;
                        object3 = new datalog$expr_clause$fn__18650();
                        break block25;
                    }
                    case 3: {
                        if (G__18645 != const__25) break;
                        object3 = const__29.getRawRoot();
                        break block25;
                    }
                }
                Object object23 = G__18645;
                G__18645 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__17.getRawRoot()).invoke((Object)"No matching clause: ", object23));
            }
            expr = null;
            object3 = f = ((IFn)const__30.getRawRoot()).invoke(sources, vars, expr, bind_type, binds);
        }
        if ((long)RT.count((Object)sources) >= 2L) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__17.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"Can't have more than one data source in expression", (Object)"\n", ((IFn)const__18.getRawRoot()).invoke(const__33))));
        }
        if (!Util.identical((Object)xtra, null)) {
            Object object24 = xtra;
            xtra = null;
            throw (Throwable)((Object)new AssertionError(((IFn)const__17.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__17.getRawRoot()).invoke((Object)"Can't have anything after binding expression: ", object24), (Object)"\n", ((IFn)const__18.getRawRoot()).invoke(const__34))));
        }
        Object[] objectArray = new Object[12];
        objectArray[0] = const__35;
        Object object25 = vars;
        vars = null;
        objectArray[1] = object25;
        objectArray[2] = const__36;
        Object object26 = f;
        f = null;
        objectArray[3] = object26;
        objectArray[4] = const__37;
        Object object27 = clause;
        clause = null;
        objectArray[5] = object27;
        objectArray[6] = const__38;
        Object object28 = binds;
        binds = null;
        objectArray[7] = object28;
        objectArray[8] = const__39;
        Object object29 = bind_type;
        bind_type = null;
        objectArray[9] = object29;
        objectArray[10] = const__40;
        Object object30 = needs_source;
        needs_source = null;
        objectArray[11] = object30;
        IPersistentMap ret = RT.mapUniqueKeys((Object[])objectArray);
        Object object31 = sources;
        if (object31 != null && object31 != Boolean.FALSE) {
            IPersistentMap iPersistentMap = ret;
            ret = null;
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__42;
            Object object32 = sources;
            sources = null;
            objectArray2[1] = ((IFn)const__8.getRawRoot()).invoke(object32);
            object = ((IFn)const__41.getRawRoot()).invoke((Object)iPersistentMap, (Object)RT.mapUniqueKeys((Object[])objectArray2));
        } else {
            object = ret;
            ret = null;
        }
        IPersistentMap ret2 = object;
        object2 = ret2;
        return object2;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$expr_clause.invokeStatic(object2);
    }
}

