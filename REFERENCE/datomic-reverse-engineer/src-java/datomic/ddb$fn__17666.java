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
 *  com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import java.util.Date;

public final class ddb$fn__17666
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"lastIncreaseDateTime");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"lastDecreaseDateTime");
    public static final Keyword const__6 = RT.keyword(null, (String)"numberOfDecreasesToday");
    public static final Keyword const__7 = RT.keyword(null, (String)"readCapacityUnits");
    public static final Keyword const__8 = RT.keyword(null, (String)"writeCapacityUnits");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        Long temp__5457__auto__17677;
        IPersistentVector iPersistentVector2;
        Long temp__5457__auto__17675;
        IPersistentVector iPersistentVector3;
        Long temp__5457__auto__17673;
        IPersistentVector iPersistentVector4;
        Date temp__5457__auto__17671;
        IPersistentVector iPersistentVector5;
        Date temp__5457__auto__17669;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Date date = temp__5457__auto__17669 = ((ProvisionedThroughputDescription)o).getLastIncreaseDateTime();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__17668;
            Date date2 = temp__5457__auto__17669;
            temp__5457__auto__17669 = null;
            Date date3 = v__17285__auto__17668 = date2;
            v__17285__auto__17668 = null;
            iPersistentVector5 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector5 = null;
        }
        Date date4 = temp__5457__auto__17671 = ((ProvisionedThroughputDescription)o).getLastDecreaseDateTime();
        if (date4 != null && date4 != Boolean.FALSE) {
            Date v__17285__auto__17670;
            Date date5 = temp__5457__auto__17671;
            temp__5457__auto__17671 = null;
            Date date6 = v__17285__auto__17670 = date5;
            v__17285__auto__17670 = null;
            iPersistentVector4 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date6));
        } else {
            iPersistentVector4 = null;
        }
        Long l = temp__5457__auto__17673 = ((ProvisionedThroughputDescription)o).getNumberOfDecreasesToday();
        if (l != null && l != Boolean.FALSE) {
            Long v__17285__auto__17672;
            Long l2 = temp__5457__auto__17673;
            temp__5457__auto__17673 = null;
            Long l3 = v__17285__auto__17672 = l2;
            v__17285__auto__17672 = null;
            iPersistentVector3 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)l3));
        } else {
            iPersistentVector3 = null;
        }
        Long l4 = temp__5457__auto__17675 = ((ProvisionedThroughputDescription)o).getReadCapacityUnits();
        if (l4 != null && l4 != Boolean.FALSE) {
            Long v__17285__auto__17674;
            Long l5 = temp__5457__auto__17675;
            temp__5457__auto__17675 = null;
            Long l6 = v__17285__auto__17674 = l5;
            v__17285__auto__17674 = null;
            iPersistentVector2 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)l6));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        Long l7 = temp__5457__auto__17677 = ((ProvisionedThroughputDescription)object2).getWriteCapacityUnits();
        if (l7 != null && l7 != Boolean.FALSE) {
            Long v__17285__auto__17676;
            Long l8 = temp__5457__auto__17677;
            temp__5457__auto__17677 = null;
            Long l9 = v__17285__auto__17676 = l8;
            v__17285__auto__17676 = null;
            iPersistentVector = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)l9));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector5, (Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17666.invokeStatic(object2);
    }
}

