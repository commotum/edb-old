/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datalog$sched_in_order$fn__18539
extends AFunction {
    Object init_binds;
    Object unpack;
    Object underbound_QMARK_;
    Object reqcnt;
    Object cbinds;
    Object pack;
    Object body;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"split-with");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"partial");
    public static final Object const__5 = 2L;
    public static final Object const__9 = 3L;
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__11 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__12 = RT.keyword((String)"db.error", (String)"insufficient-binding");
    public static final Var const__13 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__17 = RT.var((String)"datomic.datalog", (String)"free-vars");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"map?");
    public static final Keyword const__23 = RT.keyword(null, (String)"else");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"rest");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"argvars"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"clause"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public datalog$sched_in_order$fn__18539(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.init_binds = object;
        this.unpack = object2;
        this.underbound_QMARK_ = object3;
        this.reqcnt = object4;
        this.cbinds = object5;
        this.pack = object6;
        this.body = object7;
    }

    public Object invoke() {
        Object clauses = PersistentVector.EMPTY;
        this.init_binds = null;
        Object bindings = ((IFn)const__0.getRawRoot()).invoke(this.init_binds);
        this.pack = null;
        this.body = null;
        Object remclauses = ((IFn)const__1.getRawRoot()).invoke(this.pack, this.body);
        while (true) {
            Object pc;
            Object object;
            Object object2;
            Object object3 = ((IFn)const__2.getRawRoot()).invoke(remclauses);
            if (object3 != null && object3 != Boolean.FALSE) break;
            Object vec__18540 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(this.underbound_QMARK_, const__5, bindings), remclauses);
            Object skip = RT.nth((Object)vec__18540, (int)RT.uncheckedIntCast((long)0L), null);
            Object object4 = vec__18540;
            vec__18540 = null;
            Object ready = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)1L), null);
            Object object5 = ((IFn)const__2.getRawRoot()).invoke(ready);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = remclauses;
                remclauses = null;
                object2 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(this.underbound_QMARK_, const__9, bindings), object6);
            } else {
                Object object7 = skip;
                skip = null;
                Object object8 = ready;
                ready = null;
                object2 = Tuple.create((Object)object7, (Object)object8);
            }
            IPersistentVector vec__18543 = object2;
            Object skip2 = RT.nth((Object)vec__18543, (int)RT.uncheckedIntCast((long)0L), null);
            IPersistentVector iPersistentVector = vec__18543;
            vec__18543 = null;
            Object ready2 = RT.nth((Object)iPersistentVector, (int)RT.uncheckedIntCast((long)1L), null);
            Object object9 = ((IFn)const__2.getRawRoot()).invoke(ready2);
            if (object9 != null && object9 != Boolean.FALSE) {
                Object object10;
                Object c = ((IFn)this.unpack).invoke(((IFn)const__10.getRawRoot()).invoke(skip2));
                IFn iFn = (IFn)const__11.getRawRoot();
                Object object11 = ((IFn)const__13.getRawRoot()).invoke(c);
                if (object11 != null && object11 != Boolean.FALSE) {
                    Object object12 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(bindings, ((IFn)const__17.getRawRoot()).invoke(c)));
                    Object object13 = c;
                    c = null;
                    object10 = ((IFn)const__14.getRawRoot()).invoke(object12, (Object)" not bound in not clause: ", object13);
                } else {
                    Object object14 = ((IFn)this.reqcnt).invoke(c);
                    if (object14 != null && object14 != Boolean.FALSE) {
                        Object object15 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(bindings, ((IFn)const__18.getRawRoot()).invoke(((IFn)this.reqcnt).invoke(c), ((IFn)const__19.getRawRoot()).invoke(c))));
                        Object object16 = c;
                        c = null;
                        object10 = ((IFn)const__14.getRawRoot()).invoke(object15, (Object)" not bound in clause: ", object16);
                    } else {
                        Object object17 = ((IFn)const__20.getRawRoot()).invoke(c);
                        if (object17 != null && object17 != Boolean.FALSE) {
                            IFn iFn2 = (IFn)const__14.getRawRoot();
                            IFn iFn3 = (IFn)const__15.getRawRoot();
                            IFn iFn4 = (IFn)const__16.getRawRoot();
                            ILookupThunk iLookupThunk = __thunk__0__;
                            Object object18 = c;
                            Object object19 = iLookupThunk.get(object18);
                            if (iLookupThunk == object19) {
                                __thunk__0__ = __site__0__.fault(object18);
                                object19 = __thunk__0__.get(object18);
                            }
                            Object object20 = iFn3.invoke(iFn4.invoke(bindings, object19));
                            ILookupThunk iLookupThunk2 = __thunk__1__;
                            Object object21 = c;
                            c = null;
                            Object object22 = iLookupThunk2.get(object21);
                            if (iLookupThunk2 == object22) {
                                __thunk__1__ = __site__1__.fault(object21);
                                object22 = __thunk__1__.get(object21);
                            }
                            object10 = iFn2.invoke(object20, (Object)" not bound in expression clause: ", object22);
                        } else {
                            Keyword keyword = const__23;
                            if (keyword != null && keyword != Boolean.FALSE) {
                                Object object23 = c;
                                c = null;
                                object10 = ((IFn)const__14.getRawRoot()).invoke((Object)"Insufficient binding of db clause: ", object23, (Object)" would cause full scan");
                            } else {
                                object10 = null;
                            }
                        }
                    }
                }
                object = iFn.invoke((Object)const__12, object10);
            } else {
                object = null;
            }
            Object object24 = pc = ((IFn)const__10.getRawRoot()).invoke(ready2);
            pc = null;
            Object c = ((IFn)this.unpack).invoke(object24);
            Object object25 = bindings;
            bindings = null;
            Object next_bindings = ((IFn)const__24.getRawRoot()).invoke(object25, ((IFn)this.cbinds).invoke(c));
            PersistentVector persistentVector = clauses;
            clauses = null;
            Object object26 = c;
            c = null;
            Object object27 = next_bindings;
            next_bindings = null;
            Object object28 = skip2;
            skip2 = null;
            Object object29 = ready2;
            ready2 = null;
            remclauses = ((IFn)const__26.getRawRoot()).invoke(object28, ((IFn)const__27.getRawRoot()).invoke(object29));
            bindings = object27;
            clauses = ((IFn)const__25.getRawRoot()).invoke((Object)persistentVector, object26);
        }
        PersistentVector persistentVector = clauses;
        clauses = null;
        return persistentVector;
    }
}

