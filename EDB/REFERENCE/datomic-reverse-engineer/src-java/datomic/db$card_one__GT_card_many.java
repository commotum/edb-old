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
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;

public final class db$card_one__GT_card_many
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Keyword const__2 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"tuple-of-attrs-must-be-card-one");
    public static final Keyword const__4 = RT.keyword(null, (String)"attribute");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"constituent-of");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"constituent-of-a-composite-must-be-card-one");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc-in");
    public static final Keyword const__9 = RT.keyword(null, (String)"elements");
    public static final Keyword const__10 = RT.keyword(null, (String)"cardinality");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleAttrs"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object aid, Object _, Object vafter) {
        IPersistentVector iPersistentVector;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = ((IFn)const__1.getRawRoot()).invoke(db2, aid);
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__2;
            objectArray[1] = const__3;
            objectArray[2] = const__4;
            Object object4 = db2;
            db2 = null;
            Object object5 = aid;
            aid = null;
            objectArray[3] = ((Database)object4).ident(object5);
            iPersistentVector = Tuple.create((Object)object3, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
        } else {
            Object temp__5455__auto__13155;
            Object object6 = temp__5455__auto__13155 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(db2, aid));
            if (object6 != null && object6 != Boolean.FALSE) {
                temp__5455__auto__13155 = null;
                Object object7 = db2;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__2;
                objectArray[1] = const__7;
                objectArray[2] = const__4;
                Object object8 = db2;
                db2 = null;
                Object object9 = aid;
                aid = null;
                objectArray[3] = ((Database)object8).ident(object9);
                iPersistentVector = Tuple.create((Object)object7, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
            } else {
                Object object10 = db2;
                db2 = null;
                Object object11 = aid;
                aid = null;
                Object object12 = vafter;
                vafter = null;
                iPersistentVector = Tuple.create((Object)((IFn)const__8.getRawRoot()).invoke(object10, (Object)Tuple.create((Object)const__9, (Object)object11, (Object)const__10), object12));
            }
        }
        return iPersistentVector;
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
        return db$card_one__GT_card_many.invokeStatic(object5, object6, object7, object8);
    }
}

