/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.btset$bench$fn__11865;
import datomic.btset$bench$fn__11867;
import datomic.btset$bench$fn__11869;
import datomic.btset$bench$fn__11871;
import datomic.btset$bench$fn__11874;
import datomic.btset$bench$fn__11877;
import datomic.btset$bench$fn__11879;
import datomic.btset$bench$fn__11881;
import datomic.btset$bench$fn__11883;
import datomic.btset$bench$fn__11885;
import datomic.btset$bench$fn__11887;
import datomic.btset$bench$fn__11889;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

public final class btset$bench
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"prn");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__13 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"bt")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 16})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"ss")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 27}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"every?");
    public static final Object const__15 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"fn*"), Tuple.create((Object)Symbol.intern(null, (String)"p1__11862#")), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"let"), Tuple.create((Object)Symbol.intern(null, (String)"x"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"seek"), Symbol.intern(null, (String)"bt"), Symbol.intern(null, (String)"p1__11862#")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 30}))), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"x"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)".get"), Symbol.intern(null, (String)"x")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 53})), Symbol.intern(null, (String)"p1__11862#")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 50}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 43}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22})))), Symbol.intern(null, (String)"ss")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
    public static final Object const__16 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"fn*"), Tuple.create((Object)Symbol.intern(null, (String)"p1__11863#")), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"let"), Tuple.create((Object)Symbol.intern(null, (String)"x"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"rseek"), Symbol.intern(null, (String)"bt"), Symbol.intern(null, (String)"p1__11863#")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 30}))), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"x"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)".get"), Symbol.intern(null, (String)"x")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 54})), Symbol.intern(null, (String)"p1__11863#")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 51}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 44}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22})))), Symbol.intern(null, (String)"ss")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
    public static final Object const__17 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"every?"), PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"fn*"), Tuple.create((Object)Symbol.intern(null, (String)"p1__11864#")), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"contains?"), Symbol.intern(null, (String)"bt"), Symbol.intern(null, (String)"p1__11864#")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22})))), Symbol.intern(null, (String)"ss")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 13}));
    public static final Var const__18 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__20 = RT.var((String)"datomic.iter", (String)"iget");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"first");
    public static final Object const__22 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"iget"), Symbol.intern(null, (String)"bts")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 24})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), Symbol.intern(null, (String)"sss")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 35}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 21}));
    public static final Var const__23 = RT.var((String)"datomic.iter", (String)"inext");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__25 = RT.var((String)"datomic.btset", (String)"rseek");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"rseq");
    public static final Object const__27 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"iget"), Symbol.intern(null, (String)"bts")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 24})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"first"), Symbol.intern(null, (String)"sss")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 35}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 21}));
    public static final Var const__28 = RT.var((String)"datomic.iter", (String)"iprev");

    public static Object invokeStatic(Object x) {
        long _;
        Random r;
        ((IFn)const__0.getRawRoot()).invoke((Object)"sorted-set");
        long start__5856__auto__11892 = System.nanoTime();
        Random random = r = new Random(4242L);
        r = null;
        Object ret__5857__auto__11893 = ((IFn)new btset$bench$fn__11865(random, x)).invoke();
        ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11892), (double)1000000.0), (Object)" msecs"));
        Object object = ret__5857__auto__11893;
        ret__5857__auto__11893 = null;
        Object ss = object;
        ((IFn)const__0.getRawRoot()).invoke((Object)"j.u. tree set");
        long start__5856__auto__11894 = System.nanoTime();
        Random r2 = new Random(4242L);
        TreeSet ts = new TreeSet();
        Random random2 = r2;
        r2 = null;
        TreeSet treeSet = ts;
        ts = null;
        Object ret__5857__auto__11895 = ((IFn)new btset$bench$fn__11867(random2, treeSet, x)).invoke();
        ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11894), (double)1000000.0), (Object)" msecs"));
        Object object2 = ret__5857__auto__11895;
        ret__5857__auto__11895 = null;
        Object ts2 = object2;
        ((IFn)const__0.getRawRoot()).invoke((Object)"bt-set");
        long start__5856__auto__11896 = System.nanoTime();
        Random r3 = new Random(4242L);
        Object object3 = x;
        x = null;
        Random random3 = r3;
        r3 = null;
        Object ret__5857__auto__11897 = ((IFn)new btset$bench$fn__11869(object3, random3)).invoke();
        ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11896), (double)1000000.0), (Object)" msecs"));
        Object object4 = ret__5857__auto__11897;
        ret__5857__auto__11897 = null;
        Object bt = object4;
        if ((long)RT.count((Object)bt) != (long)RT.count((Object)ss)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__13))));
        }
        Object object5 = ((IFn)const__14.getRawRoot()).invoke((Object)new btset$bench$fn__11871(bt), ss);
        if (object5 == null || object5 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__15))));
        }
        Object object6 = ((IFn)const__14.getRawRoot()).invoke((Object)new btset$bench$fn__11874(bt), ss);
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__16))));
        }
        Object object7 = ((IFn)const__14.getRawRoot()).invoke((Object)new btset$bench$fn__11877(bt), ss);
        if (object7 == null || object7 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__17))));
        }
        Object bts = ((IFn)const__18.getRawRoot()).invoke(bt);
        Object sss = ((IFn)const__19.getRawRoot()).invoke(ss);
        while (true) {
            Object object8 = sss;
            if (object8 == null || object8 == Boolean.FALSE) break;
            if (!Util.equiv((Object)((IFn)const__20.getRawRoot()).invoke(bts), (Object)((IFn)const__21.getRawRoot()).invoke(sss))) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__22))));
            }
            Object object9 = bts;
            bts = null;
            Object object10 = sss;
            sss = null;
            sss = ((IFn)const__24.getRawRoot()).invoke(object10);
            bts = ((IFn)const__23.getRawRoot()).invoke(object9);
        }
        bts = ((IFn)const__25.getRawRoot()).invoke(bt);
        sss = ((IFn)const__26.getRawRoot()).invoke(ss);
        while (true) {
            Object object11 = sss;
            if (object11 == null || object11 == Boolean.FALSE) break;
            if (!Util.equiv((Object)((IFn)const__20.getRawRoot()).invoke(bts), (Object)((IFn)const__21.getRawRoot()).invoke(sss))) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__27))));
            }
            Object object12 = bts;
            bts = null;
            Object object13 = sss;
            sss = null;
            sss = ((IFn)const__24.getRawRoot()).invoke(object13);
            bts = ((IFn)const__28.getRawRoot()).invoke(object12);
        }
        ((IFn)const__0.getRawRoot()).invoke((Object)"sorted-set");
        long n__5742__auto__11900 = 20L;
        for (_ = 0L; _ < n__5742__auto__11900; ++_) {
            long start__5856__auto__11898 = System.nanoTime();
            Object ret__5857__auto__11899 = ((IFn)new btset$bench$fn__11879(ss)).invoke();
            ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11898), (double)1000000.0), (Object)" msecs"));
        }
        long n__5742__auto__11903 = 5L;
        for (_ = 0L; _ < n__5742__auto__11903; ++_) {
            long start__5856__auto__11901 = System.nanoTime();
            Object ret__5857__auto__11902 = ((IFn)new btset$bench$fn__11881(ss)).invoke();
            ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11901), (double)1000000.0), (Object)" msecs"));
        }
        ((IFn)const__0.getRawRoot()).invoke((Object)"bt-set");
        long n__5742__auto__11906 = 20L;
        for (_ = 0L; _ < n__5742__auto__11906; ++_) {
            long start__5856__auto__11904 = System.nanoTime();
            Object ret__5857__auto__11905 = ((IFn)new btset$bench$fn__11883(bt)).invoke();
            ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11904), (double)1000000.0), (Object)" msecs"));
        }
        long n__5742__auto__11909 = 5L;
        for (_ = 0L; _ < n__5742__auto__11909; ++_) {
            long start__5856__auto__11907 = System.nanoTime();
            Object ret__5857__auto__11908 = ((IFn)new btset$bench$fn__11885(bt)).invoke();
            ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11907), (double)1000000.0), (Object)" msecs"));
        }
        ((IFn)const__0.getRawRoot()).invoke((Object)"j.u. tree set");
        long n__5742__auto__11912 = 20L;
        for (_ = 0L; _ < n__5742__auto__11912; ++_) {
            long start__5856__auto__11910 = System.nanoTime();
            Object ret__5857__auto__11911 = ((IFn)new btset$bench$fn__11887(ts2)).invoke();
            ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11910), (double)1000000.0), (Object)" msecs"));
        }
        long n__5742__auto__11915 = 5L;
        for (_ = 0L; _ < n__5742__auto__11915; ++_) {
            long start__5856__auto__11913 = System.nanoTime();
            Object ret__5857__auto__11914 = ((IFn)new btset$bench$fn__11889(ts2)).invoke();
            ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)(System.nanoTime() - start__5856__auto__11913), (double)1000000.0), (Object)" msecs"));
        }
        Object object14 = ss;
        ss = null;
        return RT.count((Object)object14);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return btset$bench.invokeStatic(object2);
    }
}

