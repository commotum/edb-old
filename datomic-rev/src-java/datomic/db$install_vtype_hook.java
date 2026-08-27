/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db.IDbImpl;
import datomic.db.ValueType;
import datomic.impl.db.IDatum;

public final class db$install_vtype_hook
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"validate-hook-target");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Keyword const__2 = RT.keyword(null, (String)"raw");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword((String)"db", (String)"ident");
    public static final Keyword const__7 = RT.keyword((String)"fressian", (String)"tag");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__10 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"invalid-value-type");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__13 = RT.keyword(null, (String)"entity");

    public static Object invokeStatic(Object _, Object db2, Object d, Object check_QMARK_) {
        Object map__13044;
        Object object;
        Object object2 = check_QMARK_;
        check_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            ((IFn)const__0.getRawRoot()).invoke(db2, d);
        }
        Object object3 = d;
        d = null;
        Object id = ((IDatum)object3).getV();
        Object map__130442 = ((IFn)const__1.getRawRoot()).invoke(db2, id, (Object)const__2, (Object)Boolean.TRUE);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(map__130442);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__130442;
            map__130442 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object5)));
        } else {
            object = map__130442;
            map__130442 = null;
        }
        Object ent = map__13044 = object;
        Object key = RT.get((Object)map__13044, (Object)const__6);
        Object object6 = map__13044;
        map__13044 = null;
        Object fressian_tag = RT.get((Object)object6, (Object)const__7);
        Object object7 = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), (Object)Tuple.create((Object)key, (Object)fressian_tag));
        if (object7 != null && object7 != Boolean.FALSE) {
        } else {
            Object object8;
            Object or__5238__auto__13046;
            IFn iFn = (IFn)const__10.getRawRoot();
            IFn iFn2 = (IFn)const__12.getRawRoot();
            Object object9 = or__5238__auto__13046 = key;
            if (object9 != null && object9 != Boolean.FALSE) {
                object8 = or__5238__auto__13046;
                or__5238__auto__13046 = null;
            } else {
                object8 = id;
            }
            Object[] objectArray = new Object[2];
            objectArray[0] = const__13;
            Object object10 = ent;
            ent = null;
            objectArray[1] = object10;
            iFn.invoke((Object)const__11, iFn2.invoke((Object)"The entity ", object8, (Object)" must specify :db/ident and :fressian/tag to be installed as a valueType"), (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        Object object11 = db2;
        db2 = null;
        Object object12 = id;
        id = null;
        Object object13 = key;
        key = null;
        Object object14 = fressian_tag;
        fressian_tag = null;
        return ((IDbImpl)object11).addElement(new ValueType(object12, object13, object14));
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
        return db$install_vtype_hook.invokeStatic(object5, object6, object7, object8);
    }
}

