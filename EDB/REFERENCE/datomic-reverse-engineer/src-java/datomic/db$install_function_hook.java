/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db.Function;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;
import java.util.Arrays;

public final class db$install_function_hook
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"zero?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)".getE"), Symbol.intern(null, (String)"d")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 18}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Keyword const__5 = RT.keyword(null, (String)"raw");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__9 = RT.keyword((String)"db", (String)"ident");
    public static final Keyword const__10 = RT.keyword((String)"db", (String)"lang");
    public static final Keyword const__11 = RT.keyword((String)"db", (String)"code");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__14 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"invalid-data-function");
    public static final Keyword const__16 = RT.keyword(null, (String)"entity");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"safe-compile-function");

    public static Object invokeStatic(Object _, Object db2, Object d, Object check_QMARK_) {
        Object map__13247;
        Object object;
        if (((IDatum)d).getE() != 0L) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        Object object2 = d;
        d = null;
        Object id = ((IDatum)object2).getV();
        Object map__132472 = ((IFn)const__4.getRawRoot()).invoke(db2, id, (Object)const__5, (Object)Boolean.TRUE);
        Object object3 = ((IFn)const__6.getRawRoot()).invoke(map__132472);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__132472;
            map__132472 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__7.getRawRoot()).invoke(object4)));
        } else {
            object = map__132472;
            map__132472 = null;
        }
        Object ent = map__13247 = object;
        Object key = RT.get((Object)map__13247, (Object)const__9);
        Object lang = RT.get((Object)map__13247, (Object)const__10);
        Object object5 = map__13247;
        map__13247 = null;
        Object code = RT.get((Object)object5, (Object)const__11);
        Object object6 = check_QMARK_;
        check_QMARK_ = null;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = ((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), (Object)Tuple.create((Object)key, (Object)lang, (Object)code));
            if (object7 != null && object7 != Boolean.FALSE) {
            } else {
                Object object8;
                Object or__5238__auto__13249;
                IFn iFn = (IFn)const__14.getRawRoot();
                IFn iFn2 = (IFn)const__1.getRawRoot();
                Object object9 = or__5238__auto__13249 = key;
                if (object9 != null && object9 != Boolean.FALSE) {
                    object8 = or__5238__auto__13249;
                    or__5238__auto__13249 = null;
                } else {
                    object8 = id;
                }
                Object[] objectArray = new Object[2];
                objectArray[0] = const__16;
                Object object10 = ent;
                ent = null;
                objectArray[1] = object10;
                iFn.invoke((Object)const__15, iFn2.invoke((Object)"The entity ", object8, (Object)" must specify :db/ident, :db/lang, and :db/code to be installed as a function."), (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        }
        Object object11 = code;
        code = null;
        String code2 = ((String)object11).replaceAll("momentic", "datomic");
        Object f = ((IFn)const__17.getRawRoot()).invoke(db2, lang, (Object)code2);
        Object object12 = db2;
        db2 = null;
        Object object13 = id;
        id = null;
        Object object14 = key;
        key = null;
        Object object15 = lang;
        lang = null;
        String string = code2;
        code2 = null;
        Object object16 = f;
        f = null;
        return ((IDbImpl)object12).addElement(new Function(object13, object14, object15, string, object16));
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
        return db$install_function_hook.invokeStatic(object5, object6, object7, object8);
    }
}

