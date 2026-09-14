/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class integrity$defcrosscheck
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"clojure.core", (String)"defn");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__9 = (AFn)((IObj)Symbol.intern(null, (String)"db1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDb")}));
    public static final AFn const__10 = (AFn)((IObj)Symbol.intern(null, (String)"db2")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDb")}));
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"progress");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"start");
    public static final AFn const__14 = (AFn)Symbol.intern((String)"java.lang.System", (String)"nanoTime");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"index-pred");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"datomic.integrity", (String)"mk-index-pred");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"db2");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"keyword");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"clojure.core", (String)"loop");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"i");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final AFn const__22 = (AFn)Symbol.intern((String)"datomic.db", (String)"filter-retractions");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"db1");
    public static final AFn const__24 = (AFn)Symbol.intern((String)"datomic.db", (String)"datum");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"db1");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Keyword const__27 = RT.keyword(null, (String)"line");
    public static final Object const__28 = 402;
    public static final Keyword const__29 = RT.keyword(null, (String)"column");
    public static final Object const__30 = 21;
    public static final Keyword const__31 = RT.keyword(null, (String)"tag");
    public static final AFn const__32 = (AFn)Symbol.intern(null, (String)"datomic.iter.Iter");
    public static final AFn const__33 = (AFn)Symbol.intern(null, (String)"c");
    public static final Object const__34 = 0L;
    public static final AFn const__35 = (AFn)Symbol.intern(null, (String)"progress");
    public static final AFn const__36 = (AFn)Symbol.intern(null, (String)"c");
    public static final AFn const__37 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__38 = (AFn)Symbol.intern(null, (String)"i");
    public static final AFn const__39 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__40 = (AFn)Symbol.intern(null, (String)"datum");
    public static final AFn const__41 = (AFn)Symbol.intern(null, (String)".get");
    public static final AFn const__42 = (AFn)Symbol.intern(null, (String)"i");
    public static final AFn const__43 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__44 = (AFn)Symbol.intern(null, (String)"index-pred");
    public static final AFn const__45 = (AFn)Symbol.intern(null, (String)"datum");
    public static final AFn const__46 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__47 = (AFn)Symbol.intern((String)"clojure.core", (String)"when-not");
    public static final AFn const__48 = (AFn)Symbol.intern((String)"clojure.core", (String)"=");
    public static final AFn const__49 = (AFn)Symbol.intern(null, (String)".get");
    public static final AFn const__50 = (AFn)Symbol.intern(null, (String)"db2");
    public static final AFn const__51 = (AFn)Symbol.intern(null, (String)"datum");
    public static final AFn const__52 = (AFn)Symbol.intern(null, (String)"datum");
    public static final AFn const__53 = (AFn)Symbol.intern(null, (String)"throw");
    public static final AFn const__54 = (AFn)Symbol.intern((String)"clojure.core", (String)"ex-info");
    public static final AFn const__55 = (AFn)Symbol.intern((String)"clojure.core", (String)"str");
    public static final AFn const__56 = (AFn)Symbol.intern((String)"clojure.core", (String)"pr-str");
    public static final AFn const__57 = (AFn)Symbol.intern(null, (String)"datum");
    public static final AFn const__58 = (AFn)Symbol.intern(null, (String)"quote");
    public static final Keyword const__59 = RT.keyword(null, (String)"datom");
    public static final AFn const__60 = (AFn)Symbol.intern(null, (String)"datum");
    public static final Keyword const__61 = RT.keyword(null, (String)"db1");
    public static final AFn const__62 = (AFn)Symbol.intern(null, (String)"db2");
    public static final Keyword const__63 = RT.keyword(null, (String)"db2");
    public static final AFn const__64 = (AFn)Symbol.intern(null, (String)"db2");
    public static final AFn const__65 = (AFn)Symbol.intern(null, (String)"recur");
    public static final AFn const__66 = (AFn)Symbol.intern(null, (String)".next");
    public static final AFn const__67 = (AFn)Symbol.intern(null, (String)"i");
    public static final AFn const__68 = (AFn)Symbol.intern((String)"clojure.core", (String)"inc");
    public static final AFn const__69 = (AFn)Symbol.intern(null, (String)"c");
    public static final AFn const__70 = (AFn)Symbol.intern(null, (String)"recur");
    public static final AFn const__71 = (AFn)Symbol.intern(null, (String)".next");
    public static final AFn const__72 = (AFn)Symbol.intern(null, (String)"i");
    public static final AFn const__73 = (AFn)Symbol.intern(null, (String)"c");
    public static final Keyword const__74 = RT.keyword(null, (String)"count");
    public static final AFn const__75 = (AFn)Symbol.intern(null, (String)"c");
    public static final Keyword const__76 = RT.keyword(null, (String)"check");
    public static final Keyword const__77 = RT.keyword(null, (String)"next-t");
    public static final AFn const__78 = (AFn)Symbol.intern(null, (String)".getNextT");
    public static final AFn const__79 = (AFn)Symbol.intern(null, (String)"db1");
    public static final Keyword const__80 = RT.keyword(null, (String)"msec");
    public static final AFn const__81 = (AFn)Symbol.intern((String)"clojure.core", (String)"quot");
    public static final AFn const__82 = (AFn)Symbol.intern((String)"clojure.core", (String)"-");
    public static final AFn const__83 = (AFn)Symbol.intern((String)"java.lang.System", (String)"nanoTime");
    public static final AFn const__84 = (AFn)Symbol.intern(null, (String)"start");
    public static final Object const__85 = 1000000L;

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object i1, Object i2) {
        Object mname = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"crosscheck-", ((IFn)const__2.getRawRoot()).invoke(i1), (Object)"-", ((IFn)const__2.getRawRoot()).invoke(i2)));
        Object i1_up = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)".seek", (Object)((String)((IFn)const__2.getRawRoot()).invoke(i1)).toUpperCase()));
        Object i2_up = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)".seek", (Object)((String)((IFn)const__2.getRawRoot()).invoke(i2)).toUpperCase()));
        Object object = ((IFn)const__5.getRawRoot()).invoke(mname);
        Object object2 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__13), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__14)))), ((IFn)const__5.getRawRoot()).invoke((Object)const__15), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__16), ((IFn)const__5.getRawRoot()).invoke((Object)const__17), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(i2)))))))));
        Object object3 = i1_up;
        i1_up = null;
        Object object4 = i2_up;
        i2_up = null;
        Object object5 = i1;
        i1 = null;
        Object object6 = i2;
        i2 = null;
        Object object7 = mname;
        mname = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6), object, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__9), ((IFn)const__5.getRawRoot()).invoke((Object)const__10), ((IFn)const__5.getRawRoot()).invoke((Object)const__11))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__12), object2, ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__19), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__20), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__22), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object3), ((IFn)const__5.getRawRoot()).invoke((Object)const__23), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__24), ((IFn)const__5.getRawRoot()).invoke((Object)const__25))))))))), ((IFn)const__7.getRawRoot()).invoke(const__26.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__27), ((IFn)const__5.getRawRoot()).invoke(const__28), ((IFn)const__5.getRawRoot()).invoke((Object)const__29), ((IFn)const__5.getRawRoot()).invoke(const__30), ((IFn)const__5.getRawRoot()).invoke((Object)const__31), ((IFn)const__5.getRawRoot()).invoke((Object)const__32)))))), ((IFn)const__5.getRawRoot()).invoke((Object)const__33), ((IFn)const__5.getRawRoot()).invoke(const__34))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__35), ((IFn)const__5.getRawRoot()).invoke((Object)const__36)))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__37), ((IFn)const__5.getRawRoot()).invoke((Object)const__38), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__39), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__40), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__41), ((IFn)const__5.getRawRoot()).invoke((Object)const__42)))))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__43), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__44), ((IFn)const__5.getRawRoot()).invoke((Object)const__45)))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__46), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__47), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__48), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__49), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object4), ((IFn)const__5.getRawRoot()).invoke((Object)const__50), ((IFn)const__5.getRawRoot()).invoke((Object)const__51))))))), ((IFn)const__5.getRawRoot()).invoke((Object)const__52)))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__53), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__54), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__55), ((IFn)const__5.getRawRoot()).invoke((Object)"Found "), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__56), ((IFn)const__5.getRawRoot()).invoke((Object)const__57)))), ((IFn)const__5.getRawRoot()).invoke((Object)" in "), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__58), ((IFn)const__5.getRawRoot()).invoke(object5)))), ((IFn)const__5.getRawRoot()).invoke((Object)" but not in "), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__58), ((IFn)const__5.getRawRoot()).invoke(object6))))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__26.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__59), ((IFn)const__5.getRawRoot()).invoke((Object)const__60), ((IFn)const__5.getRawRoot()).invoke((Object)const__61), ((IFn)const__5.getRawRoot()).invoke((Object)const__62), ((IFn)const__5.getRawRoot()).invoke((Object)const__63), ((IFn)const__5.getRawRoot()).invoke((Object)const__64)))))))))))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__65), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__66), ((IFn)const__5.getRawRoot()).invoke((Object)const__67)))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__68), ((IFn)const__5.getRawRoot()).invoke((Object)const__69)))))))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__70), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__71), ((IFn)const__5.getRawRoot()).invoke((Object)const__72)))), ((IFn)const__5.getRawRoot()).invoke((Object)const__73)))))))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(const__26.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__74), ((IFn)const__5.getRawRoot()).invoke((Object)const__75), ((IFn)const__5.getRawRoot()).invoke((Object)const__76), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__58), ((IFn)const__5.getRawRoot()).invoke(object7)))), ((IFn)const__5.getRawRoot()).invoke((Object)const__77), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__78), ((IFn)const__5.getRawRoot()).invoke((Object)const__79)))), ((IFn)const__5.getRawRoot()).invoke((Object)const__80), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__81), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__82), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__83)))), ((IFn)const__5.getRawRoot()).invoke((Object)const__84)))), ((IFn)const__5.getRawRoot()).invoke(const__85)))))))))))))))))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return integrity$defcrosscheck.invokeStatic(object5, object6, object7, object8);
    }
}

