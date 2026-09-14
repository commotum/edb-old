/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.fressian.handlers.IWriteHandlerLookup
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.Db;
import org.fressian.handlers.IWriteHandlerLookup;
import org.fressian.handlers.WriteHandler;

public final class db$validated_v_for_attr
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"validated-v-for-attr");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"nil-value");
    public static final Keyword const__4 = RT.keyword(null, (String)"data");
    public static final Keyword const__5 = RT.keyword(null, (String)"attribute");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"to-kw");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"wrong-type-for-attribute");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"system-eid");
    public static final Keyword const__16 = RT.keyword((String)"db.type", (String)"tuple");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"validated-tuple");
    public static final Object const__18 = 1L;
    public static final Keyword const__19 = RT.keyword((String)"db.error", (String)"invalid-tuple-value");
    public static final Keyword const__20 = RT.keyword(null, (String)"value");
    public static final Var const__21 = RT.var((String)"datomic.db", (String)"write-handler-lookup");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"subs");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__24 = RT.keyword(null, (String)"else");
    public static final Var const__25 = RT.var((String)"datomic.db", (String)"canonicalize-v");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fressian-tag"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"fressian-tag"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"fressian-tag"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object attr, Object v, Object procargs, Object procid) {
        Object object;
        Object vt = ((Db)db2).elementAt(((Attribute)attr).vtypeid);
        if (Util.identical((Object)v, null)) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__4;
            Object object2 = procargs;
            procargs = null;
            objectArray[1] = object2;
            objectArray[2] = const__5;
            Object object3 = db2;
            db2 = null;
            Object object4 = attr;
            attr = null;
            objectArray[3] = ((IFn)const__6.getRawRoot()).invoke(object3, ((Attribute)object4).id());
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"Nil is not a legal value", (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else if (Util.equiv((Object)((Attribute)attr).vtypeid, (long)20L)) {
            Object object5 = procargs;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = db2;
                db2 = null;
                Object object7 = v;
                v = null;
                Object object8 = procargs;
                procargs = null;
                object = ((IFn)const__9.getRawRoot()).invoke(object6, object7, object8);
            } else {
                Object object9 = db2;
                db2 = null;
                Object object10 = v;
                v = null;
                object = Numbers.num((long)((IFn.OOL)const__9.getRawRoot()).invokePrim(object9, object10));
            }
        } else if (Util.equiv((Object)((Attribute)attr).vtypeid, (long)21L)) {
            Object kw = ((IFn)const__11.getRawRoot()).invoke(v);
            Object object11 = ((IFn)const__12.getRawRoot()).invoke(kw);
            if (object11 != null && object11 != Boolean.FALSE) {
                object = kw;
                kw = null;
            } else {
                IFn iFn = (IFn)const__13.getRawRoot();
                Object object12 = db2;
                db2 = null;
                Object object13 = attr;
                attr = null;
                Object object14 = ((Attribute)object13).id();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object15 = vt;
                vt = null;
                Object object16 = iLookupThunk.get(object15);
                if (iLookupThunk == object16) {
                    __thunk__0__ = __site__0__.fault(object15);
                    object16 = __thunk__0__.get(object15);
                }
                Object object17 = v;
                v = null;
                object = iFn.invoke(object12, object14, object16, object17);
            }
        } else if (Util.equiv((Object)((Attribute)attr).vtypeid, (Object)((IFn)const__15.getRawRoot()).invoke(db2, (Object)const__16))) {
            Object temp__5455__auto__13852;
            Object object18;
            Object or__5238__auto__13851;
            IFn iFn = (IFn)const__17.getRawRoot();
            Object object19 = procid;
            procid = null;
            Object object20 = or__5238__auto__13851 = object19;
            if (object20 != null && object20 != Boolean.FALSE) {
                object18 = or__5238__auto__13851;
                or__5238__auto__13851 = null;
            } else {
                object18 = const__18;
            }
            Object object21 = temp__5455__auto__13852 = iFn.invoke(db2, object18, attr, v);
            if (object21 != null && object21 != Boolean.FALSE) {
                Object tup;
                Object object22 = temp__5455__auto__13852;
                temp__5455__auto__13852 = null;
                object = tup = object22;
                tup = null;
            } else {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__20;
                Object object23 = v;
                v = null;
                objectArray[1] = object23;
                objectArray[2] = const__5;
                Object object24 = db2;
                db2 = null;
                Object object25 = attr;
                attr = null;
                objectArray[3] = ((IFn)const__6.getRawRoot()).invoke(object24, ((Attribute)object25).id());
                object = ((IFn)const__2.getRawRoot()).invoke((Object)const__19, (Object)"Invalid tuple value", (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        } else {
            IWriteHandlerLookup iWriteHandlerLookup = (IWriteHandlerLookup)const__21.getRawRoot();
            IFn iFn = (IFn)const__22.getRawRoot();
            IFn iFn2 = (IFn)const__23.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object26 = vt;
            Object object27 = iLookupThunk.get(object26);
            if (iLookupThunk == object27) {
                __thunk__1__ = __site__1__.fault(object26);
                object27 = __thunk__1__.get(object26);
            }
            WriteHandler writeHandler = iWriteHandlerLookup.getWriteHandler((String)iFn.invoke(iFn2.invoke(object27), const__18), v);
            if (writeHandler != null && writeHandler != Boolean.FALSE) {
                object = v;
                v = null;
            } else {
                Keyword keyword = const__24;
                if (keyword != null && keyword != Boolean.FALSE) {
                    IFn iFn3 = (IFn)const__25.getRawRoot();
                    Object object28 = db2;
                    db2 = null;
                    Object object29 = attr;
                    attr = null;
                    Object object30 = ((Attribute)object29).id();
                    Object object31 = v;
                    v = null;
                    ILookupThunk iLookupThunk2 = __thunk__2__;
                    Object object32 = vt;
                    vt = null;
                    Object object33 = iLookupThunk2.get(object32);
                    if (iLookupThunk2 == object33) {
                        __thunk__2__ = __site__2__.fault(object32);
                        object33 = __thunk__2__.get(object32);
                    }
                    object = iFn3.invoke(object28, object30, object31, object33);
                } else {
                    object = null;
                }
            }
        }
        return object;
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
        return db$validated_v_for_attr.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object db2, Object attr, Object v) {
        Object object = db2;
        db2 = null;
        Object object2 = attr;
        attr = null;
        Object object3 = v;
        v = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, null, null);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$validated_v_for_attr.invokeStatic(object4, object5, object6);
    }
}

