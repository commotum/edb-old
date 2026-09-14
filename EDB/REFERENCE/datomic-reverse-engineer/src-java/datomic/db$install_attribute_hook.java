/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDbImpl;
import datomic.db.IElementImpl;
import datomic.impl.db.IDatum;

public final class db$install_attribute_hook
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"validate-hook-target");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Keyword const__2 = RT.keyword(null, (String)"raw");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword((String)"db", (String)"cardinality");
    public static final Keyword const__7 = RT.keyword((String)"db.attr", (String)"preds");
    public static final Keyword const__8 = RT.keyword((String)"db", (String)"unique");
    public static final Keyword const__9 = RT.keyword((String)"db", (String)"valueType");
    public static final Keyword const__10 = RT.keyword((String)"db", (String)"index");
    public static final Keyword const__11 = RT.keyword((String)"db", (String)"tupleType");
    public static final Keyword const__12 = RT.keyword((String)"db", (String)"tupleTypes");
    public static final Keyword const__13 = RT.keyword((String)"db", (String)"fulltext");
    public static final Keyword const__14 = RT.keyword((String)"db", (String)"noHistory");
    public static final Keyword const__15 = RT.keyword((String)"db", (String)"isComponent");
    public static final Keyword const__16 = RT.keyword((String)"db", (String)"ident");
    public static final Keyword const__17 = RT.keyword((String)"db", (String)"tupleAttrs");
    public static final Var const__18 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__20 = RT.var((String)"datomic.db", (String)"BOOT-IDS");
    public static final Keyword const__21 = RT.keyword((String)"db.type", (String)"string");
    public static final Var const__22 = RT.var((String)"datomic.db", (String)"needs-avet?");
    public static final Var const__23 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__24 = RT.keyword((String)"db.error", (String)"attribute-ident-missing");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__26 = RT.keyword(null, (String)"entity");
    public static final Var const__27 = RT.var((String)"datomic.db", (String)"install-attribute-errors");
    public static final Keyword const__28 = RT.keyword((String)"db.error", (String)"invalid-install-attribute");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__31 = RT.keyword((String)"db", (String)"errors");
    public static final Var const__32 = RT.var((String)"datomic.db", (String)"value-type");
    public static final Keyword const__33 = RT.keyword((String)"db.error", (String)"not-a-value-type");
    public static final Var const__34 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Keyword const__37 = RT.keyword((String)"db.error", (String)"not-a-cardinality");
    public static final Var const__39 = RT.var((String)"datomic.db", (String)"create-attribute");
    public static final Keyword const__40 = RT.keyword(null, (String)"unique");
    public static final Keyword const__41 = RT.keyword(null, (String)"vtypeid");
    public static final Keyword const__42 = RT.keyword(null, (String)"storageHasAVET");
    public static final Keyword const__43 = RT.keyword(null, (String)"index");
    public static final Keyword const__44 = RT.keyword(null, (String)"tupleType");
    public static final Keyword const__45 = RT.keyword(null, (String)"tupleTypes");
    public static final Keyword const__46 = RT.keyword(null, (String)"fulltext");
    public static final Keyword const__47 = RT.keyword(null, (String)"noHistory");
    public static final Keyword const__48 = RT.keyword(null, (String)"isComponent");
    public static final Keyword const__49 = RT.keyword(null, (String)"kw");
    public static final Keyword const__50 = RT.keyword(null, (String)"needsAVET");
    public static final Keyword const__51 = RT.keyword(null, (String)"id");
    public static final Keyword const__52 = RT.keyword(null, (String)"tupleAttrs");
    public static final Keyword const__53 = RT.keyword(null, (String)"cardinality");
    public static final Keyword const__54 = RT.keyword(null, (String)"attrPreds");
    public static final Var const__55 = RT.var((String)"datomic.db", (String)"add-constituents");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"error"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"cloud-compat"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object before, Object after, Object d, Object check_QMARK_) {
        Object object;
        Object object2;
        Object or__5238__auto__13140;
        Object object3;
        Object or__5238__auto__13139;
        Object object4;
        Object or__5238__auto__13138;
        Object object5;
        Object and__5236__auto__13135;
        Object object6;
        Object or__5238__auto__13134;
        Object map__13132;
        Object object7;
        Object object8 = check_QMARK_;
        if (object8 != null && object8 != Boolean.FALSE) {
            ((IFn)const__0.getRawRoot()).invoke(after, d);
        }
        Object object9 = d;
        d = null;
        Object id = ((IDatum)object9).getV();
        Object map__131322 = ((IFn)const__1.getRawRoot()).invoke(after, id, (Object)const__2, (Object)Boolean.TRUE);
        Object object10 = ((IFn)const__3.getRawRoot()).invoke(map__131322);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = map__131322;
            map__131322 = null;
            object7 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object11)));
        } else {
            object7 = map__131322;
            map__131322 = null;
        }
        Object ent = map__13132 = object7;
        Object cardinality = RT.get((Object)map__13132, (Object)const__6);
        Object attrPreds = RT.get((Object)map__13132, (Object)const__7);
        Object unique = RT.get((Object)map__13132, (Object)const__8);
        Object vtypeid = RT.get((Object)map__13132, (Object)const__9);
        Object index2 = RT.get((Object)map__13132, (Object)const__10);
        Object tupleType = RT.get((Object)map__13132, (Object)const__11);
        Object tupleTypes = RT.get((Object)map__13132, (Object)const__12);
        Object fulltext2 = RT.get((Object)map__13132, (Object)const__13);
        Object noHistory = RT.get((Object)map__13132, (Object)const__14);
        Object isComponent = RT.get((Object)map__13132, (Object)const__15);
        Object kw = RT.get((Object)map__13132, (Object)const__16);
        Object object12 = map__13132;
        map__13132 = null;
        Object tupleAttrs = RT.get((Object)object12, (Object)const__17);
        Object object13 = kw;
        kw = null;
        Object object14 = or__5238__auto__13134 = object13;
        if (object14 != null && object14 != Boolean.FALSE) {
            object6 = or__5238__auto__13134;
            or__5238__auto__13134 = null;
        } else {
            object6 = ((IFn)const__18.getRawRoot()).invoke(after, id);
        }
        Object kw2 = object6;
        Object object15 = fulltext2;
        fulltext2 = null;
        Object object16 = and__5236__auto__13135 = object15;
        if (object16 != null && object16 != Boolean.FALSE) {
            object5 = Util.equiv((Object)vtypeid, (Object)RT.get((Object)const__20.getRawRoot(), (Object)const__21)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object5 = and__5236__auto__13135;
            and__5236__auto__13135 = null;
        }
        Object fulltext3 = object5;
        Object avet2 = ((IFn)const__22.getRawRoot()).invoke(ent);
        Object object17 = check_QMARK_;
        check_QMARK_ = null;
        if (object17 != null && object17 != Boolean.FALSE) {
            Object temp__5457__auto__13136;
            Object object18 = kw2;
            if (object18 != null && object18 != Boolean.FALSE) {
            } else {
                ((IFn)const__23.getRawRoot()).invoke((Object)const__24, ((IFn)const__25.getRawRoot()).invoke((Object)"Missing :db/ident for ", ent), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__26, ent}));
            }
            Object object19 = temp__5457__auto__13136 = ((IFn)const__27.getRawRoot()).invoke(before, after, id);
            if (object19 != null && object19 != Boolean.FALSE) {
                Object object20 = temp__5457__auto__13136;
                temp__5457__auto__13136 = null;
                Object errors = object20;
                IFn iFn = (IFn)const__23.getRawRoot();
                IFn iFn2 = (IFn)const__25.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object21 = ((IFn)const__30.getRawRoot()).invoke(errors);
                Object object22 = iLookupThunk.get(object21);
                if (iLookupThunk == object22) {
                    __thunk__0__ = __site__0__.fault(object21);
                    object22 = __thunk__0__.get(object21);
                }
                Object[] objectArray = new Object[2];
                objectArray[0] = const__31;
                Object object23 = errors;
                errors = null;
                objectArray[1] = object23;
                iFn.invoke((Object)const__28, iFn2.invoke((Object)"First error: ", object22), (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            Object object24 = ((IFn)const__32.getRawRoot()).invoke(before, vtypeid);
            if (object24 != null && object24 != Boolean.FALSE) {
            } else {
                ((IFn)const__23.getRawRoot()).invoke((Object)const__33, ((IFn)const__25.getRawRoot()).invoke((Object)"Not a value type: ", ((IFn)const__34.getRawRoot()).invoke(before, vtypeid)), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__26, ent}));
            }
            boolean or__5238__auto__13137 = Util.equiv((long)36L, (Object)cardinality);
            if (or__5238__auto__13137 ? or__5238__auto__13137 : Util.equiv((long)35L, (Object)cardinality)) {
            } else {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__26;
                Object object25 = ent;
                ent = null;
                objectArray[1] = object25;
                ((IFn)const__23.getRawRoot()).invoke((Object)const__37, ((IFn)const__25.getRawRoot()).invoke((Object)"Not a cardinality: ", ((IFn)const__34.getRawRoot()).invoke(before, cardinality)), (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        }
        ILookupThunk iLookupThunk = __thunk__1__;
        Object object26 = before;
        before = null;
        Object object27 = iLookupThunk.get(object26);
        if (iLookupThunk == object27) {
            __thunk__1__ = __site__1__.fault(object26);
            object27 = __thunk__1__.get(object26);
        }
        Object always_indexed = object27;
        IFn iFn = (IFn)const__39.getRawRoot();
        Object[] objectArray = new Object[30];
        objectArray[0] = const__40;
        Object object28 = unique;
        unique = null;
        objectArray[1] = object28;
        objectArray[2] = const__41;
        Object object29 = vtypeid;
        vtypeid = null;
        objectArray[3] = object29;
        objectArray[4] = const__42;
        Object object30 = or__5238__auto__13138 = avet2;
        if (object30 != null && object30 != Boolean.FALSE) {
            object4 = or__5238__auto__13138;
            or__5238__auto__13138 = null;
        } else {
            object4 = always_indexed;
        }
        objectArray[5] = object4;
        objectArray[6] = const__43;
        Object object31 = index2;
        index2 = null;
        Object object32 = or__5238__auto__13139 = object31;
        if (object32 != null && object32 != Boolean.FALSE) {
            object3 = or__5238__auto__13139;
            or__5238__auto__13139 = null;
        } else {
            object3 = always_indexed;
        }
        objectArray[7] = object3;
        objectArray[8] = const__44;
        Object object33 = tupleType;
        tupleType = null;
        objectArray[9] = object33;
        objectArray[10] = const__45;
        Object object34 = tupleTypes;
        tupleTypes = null;
        objectArray[11] = object34;
        objectArray[12] = const__46;
        Object object35 = fulltext3;
        fulltext3 = null;
        objectArray[13] = object35;
        objectArray[14] = const__47;
        Object object36 = noHistory;
        noHistory = null;
        objectArray[15] = object36;
        objectArray[16] = const__48;
        Object object37 = isComponent;
        isComponent = null;
        objectArray[17] = object37;
        objectArray[18] = const__49;
        Object object38 = kw2;
        kw2 = null;
        objectArray[19] = object38;
        objectArray[20] = const__50;
        Object object39 = avet2;
        avet2 = null;
        Object object40 = or__5238__auto__13140 = object39;
        if (object40 != null && object40 != Boolean.FALSE) {
            object2 = or__5238__auto__13140;
            or__5238__auto__13140 = null;
        } else {
            object2 = always_indexed;
            always_indexed = null;
        }
        objectArray[21] = object2;
        objectArray[22] = const__51;
        objectArray[23] = id;
        objectArray[24] = const__52;
        objectArray[25] = tupleAttrs;
        objectArray[26] = const__53;
        Object object41 = cardinality;
        cardinality = null;
        objectArray[27] = object41;
        objectArray[28] = const__54;
        Object object42 = attrPreds;
        attrPreds = null;
        objectArray[29] = object42;
        Object attr = iFn.invoke(after, (Object)RT.mapUniqueKeys((Object[])objectArray));
        Object object43 = after;
        after = null;
        Object object44 = attr;
        attr = null;
        Object db2 = ((IDbImpl)object43).addElement((IElementImpl)object44);
        Object object45 = tupleAttrs;
        if (object45 != null && object45 != Boolean.FALSE) {
            Object object46 = db2;
            db2 = null;
            Object object47 = id;
            id = null;
            Object object48 = tupleAttrs;
            tupleAttrs = null;
            object = ((IFn)const__55.getRawRoot()).invoke(object46, object47, object48);
        } else {
            object = db2;
            db2 = null;
        }
        return object;
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
        return db$install_attribute_hook.invokeStatic(object5, object6, object7, object8);
    }
}

