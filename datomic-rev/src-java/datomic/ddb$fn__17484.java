/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.dynamodbv2.model.AttributeValue
 *  com.amazonaws.services.dynamodbv2.model.ComparisonOperator
 *  com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import java.util.Collection;

public final class ddb$fn__17484
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"exists");
    public static final Keyword const__3 = RT.keyword(null, (String)"comparisonOperator");
    public static final Keyword const__4 = RT.keyword(null, (String)"attributeValueList");
    public static final Keyword const__5 = RT.keyword(null, (String)"value");
    public static final AFn const__6 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"exists"), RT.keyword(null, (String)"comparisonOperator"), RT.keyword(null, (String)"attributeValueList"), RT.keyword(null, (String)"value")});
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__11 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__12 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"exists"), RT.keyword(null, (String)"comparisonOperator"), RT.keyword(null, (String)"attributeValueList"), RT.keyword(null, (String)"value")});
    public static final Keyword const__13 = RT.keyword(null, (String)"keys");
    public static final Keyword const__14 = RT.keyword(null, (String)"constructor");
    public static final Object const__15 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__17 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__19 = RT.classForName((String)"java.lang.Boolean");
    public static final Object const__20 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.ComparisonOperator");
    public static final Object const__21 = RT.classForName((String)"java.util.Collection");
    public static final Object const__22 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.AttributeValue");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"exists"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"comparisonOperator"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"attributeValueList"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__17486;
        Object object = temp__5457__auto__17486 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__6, ((IFn)const__7.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__17486;
            temp__5457__auto__17486 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__11;
            objectArray[1] = const__12;
            objectArray[2] = const__13;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__14;
            objectArray[5] = const__15;
            throw (Throwable)((IFn)const__8.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        ExpectedAttributeValue o = new ExpectedAttributeValue();
        Object object5 = ((IFn)const__16.getRawRoot()).invoke(m, (Object)const__2);
        if (object5 != null && object5 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = m;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            Object object8 = v = object7;
            v = null;
            Object object9 = k = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)o), (Object)const__2, object8, const__19);
            k = null;
            o.setExists((Boolean)object9);
        }
        Object object10 = ((IFn)const__16.getRawRoot()).invoke(m, (Object)const__3);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object11 = m;
            Object object12 = iLookupThunk.get(object11);
            if (iLookupThunk == object12) {
                __thunk__1__ = __site__1__.fault(object11);
                object12 = __thunk__1__.get(object11);
            }
            Object object13 = v = object12;
            v = null;
            Object object14 = k = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)o), (Object)const__3, object13, const__20);
            k = null;
            o.setComparisonOperator((ComparisonOperator)object14);
        }
        Object object15 = ((IFn)const__16.getRawRoot()).invoke(m, (Object)const__4);
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object16 = m;
            Object object17 = iLookupThunk.get(object16);
            if (iLookupThunk == object17) {
                __thunk__2__ = __site__2__.fault(object16);
                object17 = __thunk__2__.get(object16);
            }
            Object object18 = v = object17;
            v = null;
            Object object19 = k = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)o), (Object)const__4, object18, const__21);
            k = null;
            o.setAttributeValueList((Collection)object19);
        }
        Object object20 = ((IFn)const__16.getRawRoot()).invoke(m, (Object)const__5);
        if (object20 != null && object20 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__3__;
            Object object21 = m;
            m = null;
            Object object22 = iLookupThunk.get(object21);
            if (iLookupThunk == object22) {
                __thunk__3__ = __site__3__.fault(object21);
                object22 = __thunk__3__.get(object21);
            }
            Object object23 = v = object22;
            v = null;
            Object object24 = k = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)o), (Object)const__5, object23, const__22);
            k = null;
            o.setValue((AttributeValue)object24);
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$fn__17484.invokeStatic(object3, object4);
    }
}

