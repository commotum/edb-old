/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.services.dynamodbv2.model.AttributeValue
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public final class ddb$fn__17556
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"b");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"m");
    public static final Keyword const__6 = RT.keyword(null, (String)"s");
    public static final Keyword const__7 = RT.keyword(null, (String)"n");
    public static final Keyword const__8 = RT.keyword(null, (String)"sS");
    public static final Keyword const__9 = RT.keyword(null, (String)"nS");
    public static final Keyword const__10 = RT.keyword(null, (String)"bS");
    public static final Keyword const__11 = RT.keyword(null, (String)"l");
    public static final Keyword const__12 = RT.keyword(null, (String)"nULL");
    public static final Keyword const__13 = RT.keyword(null, (String)"bOOL");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        Boolean temp__5457__auto__17581;
        IPersistentVector iPersistentVector2;
        Boolean temp__5457__auto__17579;
        IPersistentVector iPersistentVector3;
        Boolean temp__5457__auto__17577;
        IPersistentVector iPersistentVector4;
        Boolean temp__5457__auto__17575;
        IPersistentVector iPersistentVector5;
        List temp__5457__auto__17573;
        IPersistentVector iPersistentVector6;
        List temp__5457__auto__17571;
        IPersistentVector iPersistentVector7;
        List temp__5457__auto__17569;
        IPersistentVector iPersistentVector8;
        List temp__5457__auto__17567;
        IPersistentVector iPersistentVector9;
        String temp__5457__auto__17565;
        IPersistentVector iPersistentVector10;
        String temp__5457__auto__17563;
        IPersistentVector iPersistentVector11;
        Map temp__5457__auto__17561;
        IPersistentVector iPersistentVector12;
        ByteBuffer temp__5457__auto__17559;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        ByteBuffer byteBuffer = temp__5457__auto__17559 = ((AttributeValue)o).getB();
        if (byteBuffer != null && byteBuffer != Boolean.FALSE) {
            ByteBuffer v__17285__auto__17558;
            ByteBuffer byteBuffer2 = temp__5457__auto__17559;
            temp__5457__auto__17559 = null;
            ByteBuffer byteBuffer3 = v__17285__auto__17558 = byteBuffer2;
            v__17285__auto__17558 = null;
            iPersistentVector12 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)byteBuffer3));
        } else {
            iPersistentVector12 = null;
        }
        Map map2 = temp__5457__auto__17561 = ((AttributeValue)o).getM();
        if (map2 != null && map2 != Boolean.FALSE) {
            Map v__17285__auto__17560;
            Map map3 = temp__5457__auto__17561;
            temp__5457__auto__17561 = null;
            Map map4 = v__17285__auto__17560 = map3;
            v__17285__auto__17560 = null;
            iPersistentVector11 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map4));
        } else {
            iPersistentVector11 = null;
        }
        String string = temp__5457__auto__17563 = ((AttributeValue)o).getS();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__17562;
            String string2 = temp__5457__auto__17563;
            temp__5457__auto__17563 = null;
            String string3 = v__17285__auto__17562 = string2;
            v__17285__auto__17562 = null;
            iPersistentVector10 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector10 = null;
        }
        String string4 = temp__5457__auto__17565 = ((AttributeValue)o).getN();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__17564;
            String string5 = temp__5457__auto__17565;
            temp__5457__auto__17565 = null;
            String string6 = v__17285__auto__17564 = string5;
            v__17285__auto__17564 = null;
            iPersistentVector9 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector9 = null;
        }
        List list = temp__5457__auto__17567 = ((AttributeValue)o).getSS();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__17566;
            List list2 = temp__5457__auto__17567;
            temp__5457__auto__17567 = null;
            List list3 = v__17285__auto__17566 = list2;
            v__17285__auto__17566 = null;
            iPersistentVector8 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector8 = null;
        }
        List list4 = temp__5457__auto__17569 = ((AttributeValue)o).getNS();
        if (list4 != null && list4 != Boolean.FALSE) {
            List v__17285__auto__17568;
            List list5 = temp__5457__auto__17569;
            temp__5457__auto__17569 = null;
            List list6 = v__17285__auto__17568 = list5;
            v__17285__auto__17568 = null;
            iPersistentVector7 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list6));
        } else {
            iPersistentVector7 = null;
        }
        List list7 = temp__5457__auto__17571 = ((AttributeValue)o).getBS();
        if (list7 != null && list7 != Boolean.FALSE) {
            List v__17285__auto__17570;
            List list8 = temp__5457__auto__17571;
            temp__5457__auto__17571 = null;
            List list9 = v__17285__auto__17570 = list8;
            v__17285__auto__17570 = null;
            iPersistentVector6 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list9));
        } else {
            iPersistentVector6 = null;
        }
        List list10 = temp__5457__auto__17573 = ((AttributeValue)o).getL();
        if (list10 != null && list10 != Boolean.FALSE) {
            List v__17285__auto__17572;
            List list11 = temp__5457__auto__17573;
            temp__5457__auto__17573 = null;
            List list12 = v__17285__auto__17572 = list11;
            v__17285__auto__17572 = null;
            iPersistentVector5 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list12));
        } else {
            iPersistentVector5 = null;
        }
        Boolean bl = temp__5457__auto__17575 = ((AttributeValue)o).getNULL();
        if (bl != null && bl != Boolean.FALSE) {
            Boolean v__17285__auto__17574;
            Boolean bl2 = temp__5457__auto__17575;
            temp__5457__auto__17575 = null;
            Boolean bl3 = v__17285__auto__17574 = bl2;
            v__17285__auto__17574 = null;
            iPersistentVector4 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl3));
        } else {
            iPersistentVector4 = null;
        }
        Boolean bl4 = temp__5457__auto__17577 = ((AttributeValue)o).isNULL();
        if (bl4 != null && bl4 != Boolean.FALSE) {
            Boolean v__17285__auto__17576;
            Boolean bl5 = temp__5457__auto__17577;
            temp__5457__auto__17577 = null;
            Boolean bl6 = v__17285__auto__17576 = bl5;
            v__17285__auto__17576 = null;
            iPersistentVector3 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl6));
        } else {
            iPersistentVector3 = null;
        }
        Boolean bl7 = temp__5457__auto__17579 = ((AttributeValue)o).getBOOL();
        if (bl7 != null && bl7 != Boolean.FALSE) {
            Boolean v__17285__auto__17578;
            Boolean bl8 = temp__5457__auto__17579;
            temp__5457__auto__17579 = null;
            Boolean bl9 = v__17285__auto__17578 = bl8;
            v__17285__auto__17578 = null;
            iPersistentVector2 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl9));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        Boolean bl10 = temp__5457__auto__17581 = ((AttributeValue)object2).isBOOL();
        if (bl10 != null && bl10 != Boolean.FALSE) {
            Boolean v__17285__auto__17580;
            Boolean bl11 = temp__5457__auto__17581;
            temp__5457__auto__17581 = null;
            Boolean bl12 = v__17285__auto__17580 = bl11;
            v__17285__auto__17580 = null;
            iPersistentVector = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl12));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector12, (Object)iPersistentVector11, (Object)iPersistentVector10, (Object)iPersistentVector9, (Object)iPersistentVector8, (Object)iPersistentVector7, (Object)iPersistentVector6, (Object)iPersistentVector5, (Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17556.invokeStatic(object2);
    }
}

