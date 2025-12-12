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
 *  com.amazonaws.services.dynamodbv2.model.ArchivalSummary
 *  com.amazonaws.services.dynamodbv2.model.BillingModeSummary
 *  com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription
 *  com.amazonaws.services.dynamodbv2.model.RestoreSummary
 *  com.amazonaws.services.dynamodbv2.model.SSEDescription
 *  com.amazonaws.services.dynamodbv2.model.StreamSpecification
 *  com.amazonaws.services.dynamodbv2.model.TableClassSummary
 *  com.amazonaws.services.dynamodbv2.model.TableDescription
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.ArchivalSummary;
import com.amazonaws.services.dynamodbv2.model.BillingModeSummary;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.services.dynamodbv2.model.RestoreSummary;
import com.amazonaws.services.dynamodbv2.model.SSEDescription;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.TableClassSummary;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import java.util.Date;
import java.util.List;

public final class ddb$fn__17616
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"itemCount");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__6 = RT.keyword(null, (String)"tableStatus");
    public static final Keyword const__7 = RT.keyword(null, (String)"creationDateTime");
    public static final Keyword const__8 = RT.keyword(null, (String)"tableSizeBytes");
    public static final Keyword const__9 = RT.keyword(null, (String)"tableArn");
    public static final Keyword const__10 = RT.keyword(null, (String)"tableId");
    public static final Keyword const__11 = RT.keyword(null, (String)"billingModeSummary");
    public static final Keyword const__12 = RT.keyword(null, (String)"latestStreamLabel");
    public static final Keyword const__13 = RT.keyword(null, (String)"latestStreamArn");
    public static final Keyword const__14 = RT.keyword(null, (String)"globalTableVersion");
    public static final Keyword const__15 = RT.keyword(null, (String)"replicas");
    public static final Keyword const__16 = RT.keyword(null, (String)"restoreSummary");
    public static final Keyword const__17 = RT.keyword(null, (String)"sSEDescription");
    public static final Keyword const__18 = RT.keyword(null, (String)"archivalSummary");
    public static final Keyword const__19 = RT.keyword(null, (String)"tableClassSummary");
    public static final Keyword const__20 = RT.keyword(null, (String)"attributeDefinitions");
    public static final Keyword const__21 = RT.keyword(null, (String)"keySchema");
    public static final Keyword const__22 = RT.keyword(null, (String)"localSecondaryIndexes");
    public static final Keyword const__23 = RT.keyword(null, (String)"globalSecondaryIndexes");
    public static final Keyword const__24 = RT.keyword(null, (String)"provisionedThroughput");
    public static final Keyword const__25 = RT.keyword(null, (String)"streamSpecification");
    public static final Keyword const__26 = RT.keyword(null, (String)"deletionProtectionEnabled");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        Boolean temp__5457__auto__17665;
        IPersistentVector iPersistentVector2;
        Boolean temp__5457__auto__17663;
        IPersistentVector iPersistentVector3;
        StreamSpecification temp__5457__auto__17661;
        IPersistentVector iPersistentVector4;
        ProvisionedThroughputDescription temp__5457__auto__17659;
        IPersistentVector iPersistentVector5;
        List temp__5457__auto__17657;
        IPersistentVector iPersistentVector6;
        List temp__5457__auto__17655;
        IPersistentVector iPersistentVector7;
        List temp__5457__auto__17653;
        IPersistentVector iPersistentVector8;
        List temp__5457__auto__17651;
        IPersistentVector iPersistentVector9;
        TableClassSummary temp__5457__auto__17649;
        IPersistentVector iPersistentVector10;
        ArchivalSummary temp__5457__auto__17647;
        IPersistentVector iPersistentVector11;
        SSEDescription temp__5457__auto__17645;
        IPersistentVector iPersistentVector12;
        RestoreSummary temp__5457__auto__17643;
        IPersistentVector iPersistentVector13;
        List temp__5457__auto__17641;
        IPersistentVector iPersistentVector14;
        String temp__5457__auto__17639;
        IPersistentVector iPersistentVector15;
        String temp__5457__auto__17637;
        IPersistentVector iPersistentVector16;
        String temp__5457__auto__17635;
        IPersistentVector iPersistentVector17;
        BillingModeSummary temp__5457__auto__17633;
        IPersistentVector iPersistentVector18;
        String temp__5457__auto__17631;
        IPersistentVector iPersistentVector19;
        String temp__5457__auto__17629;
        IPersistentVector iPersistentVector20;
        Long temp__5457__auto__17627;
        IPersistentVector iPersistentVector21;
        Date temp__5457__auto__17625;
        IPersistentVector iPersistentVector22;
        String temp__5457__auto__17623;
        IPersistentVector iPersistentVector23;
        String temp__5457__auto__17621;
        IPersistentVector iPersistentVector24;
        Long temp__5457__auto__17619;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Long l = temp__5457__auto__17619 = ((TableDescription)o).getItemCount();
        if (l != null && l != Boolean.FALSE) {
            Long v__17285__auto__17618;
            Long l2 = temp__5457__auto__17619;
            temp__5457__auto__17619 = null;
            Long l3 = v__17285__auto__17618 = l2;
            v__17285__auto__17618 = null;
            iPersistentVector24 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)l3));
        } else {
            iPersistentVector24 = null;
        }
        String string = temp__5457__auto__17621 = ((TableDescription)o).getTableName();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__17620;
            String string2 = temp__5457__auto__17621;
            temp__5457__auto__17621 = null;
            String string3 = v__17285__auto__17620 = string2;
            v__17285__auto__17620 = null;
            iPersistentVector23 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector23 = null;
        }
        String string4 = temp__5457__auto__17623 = ((TableDescription)o).getTableStatus();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__17622;
            String string5 = temp__5457__auto__17623;
            temp__5457__auto__17623 = null;
            String string6 = v__17285__auto__17622 = string5;
            v__17285__auto__17622 = null;
            iPersistentVector22 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector22 = null;
        }
        Date date = temp__5457__auto__17625 = ((TableDescription)o).getCreationDateTime();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__17624;
            Date date2 = temp__5457__auto__17625;
            temp__5457__auto__17625 = null;
            Date date3 = v__17285__auto__17624 = date2;
            v__17285__auto__17624 = null;
            iPersistentVector21 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector21 = null;
        }
        Long l4 = temp__5457__auto__17627 = ((TableDescription)o).getTableSizeBytes();
        if (l4 != null && l4 != Boolean.FALSE) {
            Long v__17285__auto__17626;
            Long l5 = temp__5457__auto__17627;
            temp__5457__auto__17627 = null;
            Long l6 = v__17285__auto__17626 = l5;
            v__17285__auto__17626 = null;
            iPersistentVector20 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)l6));
        } else {
            iPersistentVector20 = null;
        }
        String string7 = temp__5457__auto__17629 = ((TableDescription)o).getTableArn();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__17628;
            String string8 = temp__5457__auto__17629;
            temp__5457__auto__17629 = null;
            String string9 = v__17285__auto__17628 = string8;
            v__17285__auto__17628 = null;
            iPersistentVector19 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector19 = null;
        }
        String string10 = temp__5457__auto__17631 = ((TableDescription)o).getTableId();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__17630;
            String string11 = temp__5457__auto__17631;
            temp__5457__auto__17631 = null;
            String string12 = v__17285__auto__17630 = string11;
            v__17285__auto__17630 = null;
            iPersistentVector18 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector18 = null;
        }
        BillingModeSummary billingModeSummary = temp__5457__auto__17633 = ((TableDescription)o).getBillingModeSummary();
        if (billingModeSummary != null && billingModeSummary != Boolean.FALSE) {
            BillingModeSummary v__17285__auto__17632;
            BillingModeSummary billingModeSummary2 = temp__5457__auto__17633;
            temp__5457__auto__17633 = null;
            BillingModeSummary billingModeSummary3 = v__17285__auto__17632 = billingModeSummary2;
            v__17285__auto__17632 = null;
            iPersistentVector17 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)billingModeSummary3));
        } else {
            iPersistentVector17 = null;
        }
        String string13 = temp__5457__auto__17635 = ((TableDescription)o).getLatestStreamLabel();
        if (string13 != null && string13 != Boolean.FALSE) {
            String v__17285__auto__17634;
            String string14 = temp__5457__auto__17635;
            temp__5457__auto__17635 = null;
            String string15 = v__17285__auto__17634 = string14;
            v__17285__auto__17634 = null;
            iPersistentVector16 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string15));
        } else {
            iPersistentVector16 = null;
        }
        String string16 = temp__5457__auto__17637 = ((TableDescription)o).getLatestStreamArn();
        if (string16 != null && string16 != Boolean.FALSE) {
            String v__17285__auto__17636;
            String string17 = temp__5457__auto__17637;
            temp__5457__auto__17637 = null;
            String string18 = v__17285__auto__17636 = string17;
            v__17285__auto__17636 = null;
            iPersistentVector15 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string18));
        } else {
            iPersistentVector15 = null;
        }
        String string19 = temp__5457__auto__17639 = ((TableDescription)o).getGlobalTableVersion();
        if (string19 != null && string19 != Boolean.FALSE) {
            String v__17285__auto__17638;
            String string20 = temp__5457__auto__17639;
            temp__5457__auto__17639 = null;
            String string21 = v__17285__auto__17638 = string20;
            v__17285__auto__17638 = null;
            iPersistentVector14 = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string21));
        } else {
            iPersistentVector14 = null;
        }
        List list = temp__5457__auto__17641 = ((TableDescription)o).getReplicas();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__17640;
            List list2 = temp__5457__auto__17641;
            temp__5457__auto__17641 = null;
            List list3 = v__17285__auto__17640 = list2;
            v__17285__auto__17640 = null;
            iPersistentVector13 = Tuple.create((Object)const__15, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector13 = null;
        }
        RestoreSummary restoreSummary = temp__5457__auto__17643 = ((TableDescription)o).getRestoreSummary();
        if (restoreSummary != null && restoreSummary != Boolean.FALSE) {
            RestoreSummary v__17285__auto__17642;
            RestoreSummary restoreSummary2 = temp__5457__auto__17643;
            temp__5457__auto__17643 = null;
            RestoreSummary restoreSummary3 = v__17285__auto__17642 = restoreSummary2;
            v__17285__auto__17642 = null;
            iPersistentVector12 = Tuple.create((Object)const__16, (Object)((IFn)const__4.getRawRoot()).invoke((Object)restoreSummary3));
        } else {
            iPersistentVector12 = null;
        }
        SSEDescription sSEDescription = temp__5457__auto__17645 = ((TableDescription)o).getSSEDescription();
        if (sSEDescription != null && sSEDescription != Boolean.FALSE) {
            SSEDescription v__17285__auto__17644;
            SSEDescription sSEDescription2 = temp__5457__auto__17645;
            temp__5457__auto__17645 = null;
            SSEDescription sSEDescription3 = v__17285__auto__17644 = sSEDescription2;
            v__17285__auto__17644 = null;
            iPersistentVector11 = Tuple.create((Object)const__17, (Object)((IFn)const__4.getRawRoot()).invoke((Object)sSEDescription3));
        } else {
            iPersistentVector11 = null;
        }
        ArchivalSummary archivalSummary = temp__5457__auto__17647 = ((TableDescription)o).getArchivalSummary();
        if (archivalSummary != null && archivalSummary != Boolean.FALSE) {
            ArchivalSummary v__17285__auto__17646;
            ArchivalSummary archivalSummary2 = temp__5457__auto__17647;
            temp__5457__auto__17647 = null;
            ArchivalSummary archivalSummary3 = v__17285__auto__17646 = archivalSummary2;
            v__17285__auto__17646 = null;
            iPersistentVector10 = Tuple.create((Object)const__18, (Object)((IFn)const__4.getRawRoot()).invoke((Object)archivalSummary3));
        } else {
            iPersistentVector10 = null;
        }
        TableClassSummary tableClassSummary = temp__5457__auto__17649 = ((TableDescription)o).getTableClassSummary();
        if (tableClassSummary != null && tableClassSummary != Boolean.FALSE) {
            TableClassSummary v__17285__auto__17648;
            TableClassSummary tableClassSummary2 = temp__5457__auto__17649;
            temp__5457__auto__17649 = null;
            TableClassSummary tableClassSummary3 = v__17285__auto__17648 = tableClassSummary2;
            v__17285__auto__17648 = null;
            iPersistentVector9 = Tuple.create((Object)const__19, (Object)((IFn)const__4.getRawRoot()).invoke((Object)tableClassSummary3));
        } else {
            iPersistentVector9 = null;
        }
        List list4 = temp__5457__auto__17651 = ((TableDescription)o).getAttributeDefinitions();
        if (list4 != null && list4 != Boolean.FALSE) {
            List v__17285__auto__17650;
            List list5 = temp__5457__auto__17651;
            temp__5457__auto__17651 = null;
            List list6 = v__17285__auto__17650 = list5;
            v__17285__auto__17650 = null;
            iPersistentVector8 = Tuple.create((Object)const__20, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list6));
        } else {
            iPersistentVector8 = null;
        }
        List list7 = temp__5457__auto__17653 = ((TableDescription)o).getKeySchema();
        if (list7 != null && list7 != Boolean.FALSE) {
            List v__17285__auto__17652;
            List list8 = temp__5457__auto__17653;
            temp__5457__auto__17653 = null;
            List list9 = v__17285__auto__17652 = list8;
            v__17285__auto__17652 = null;
            iPersistentVector7 = Tuple.create((Object)const__21, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list9));
        } else {
            iPersistentVector7 = null;
        }
        List list10 = temp__5457__auto__17655 = ((TableDescription)o).getLocalSecondaryIndexes();
        if (list10 != null && list10 != Boolean.FALSE) {
            List v__17285__auto__17654;
            List list11 = temp__5457__auto__17655;
            temp__5457__auto__17655 = null;
            List list12 = v__17285__auto__17654 = list11;
            v__17285__auto__17654 = null;
            iPersistentVector6 = Tuple.create((Object)const__22, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list12));
        } else {
            iPersistentVector6 = null;
        }
        List list13 = temp__5457__auto__17657 = ((TableDescription)o).getGlobalSecondaryIndexes();
        if (list13 != null && list13 != Boolean.FALSE) {
            List v__17285__auto__17656;
            List list14 = temp__5457__auto__17657;
            temp__5457__auto__17657 = null;
            List list15 = v__17285__auto__17656 = list14;
            v__17285__auto__17656 = null;
            iPersistentVector5 = Tuple.create((Object)const__23, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list15));
        } else {
            iPersistentVector5 = null;
        }
        Object[] objectArray = new Object[4];
        ProvisionedThroughputDescription provisionedThroughputDescription = temp__5457__auto__17659 = ((TableDescription)o).getProvisionedThroughput();
        if (provisionedThroughputDescription != null && provisionedThroughputDescription != Boolean.FALSE) {
            ProvisionedThroughputDescription v__17285__auto__17658;
            ProvisionedThroughputDescription provisionedThroughputDescription2 = temp__5457__auto__17659;
            temp__5457__auto__17659 = null;
            ProvisionedThroughputDescription provisionedThroughputDescription3 = v__17285__auto__17658 = provisionedThroughputDescription2;
            v__17285__auto__17658 = null;
            iPersistentVector4 = Tuple.create((Object)const__24, (Object)((IFn)const__4.getRawRoot()).invoke((Object)provisionedThroughputDescription3));
        } else {
            iPersistentVector4 = null;
        }
        objectArray[0] = iPersistentVector4;
        StreamSpecification streamSpecification = temp__5457__auto__17661 = ((TableDescription)o).getStreamSpecification();
        if (streamSpecification != null && streamSpecification != Boolean.FALSE) {
            StreamSpecification v__17285__auto__17660;
            StreamSpecification streamSpecification2 = temp__5457__auto__17661;
            temp__5457__auto__17661 = null;
            StreamSpecification streamSpecification3 = v__17285__auto__17660 = streamSpecification2;
            v__17285__auto__17660 = null;
            iPersistentVector3 = Tuple.create((Object)const__25, (Object)((IFn)const__4.getRawRoot()).invoke((Object)streamSpecification3));
        } else {
            iPersistentVector3 = null;
        }
        objectArray[1] = iPersistentVector3;
        Boolean bl = temp__5457__auto__17663 = ((TableDescription)o).getDeletionProtectionEnabled();
        if (bl != null && bl != Boolean.FALSE) {
            Boolean v__17285__auto__17662;
            Boolean bl2 = temp__5457__auto__17663;
            temp__5457__auto__17663 = null;
            Boolean bl3 = v__17285__auto__17662 = bl2;
            v__17285__auto__17662 = null;
            iPersistentVector2 = Tuple.create((Object)const__26, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl3));
        } else {
            iPersistentVector2 = null;
        }
        objectArray[2] = iPersistentVector2;
        Object object2 = o;
        o = null;
        Boolean bl4 = temp__5457__auto__17665 = ((TableDescription)object2).isDeletionProtectionEnabled();
        if (bl4 != null && bl4 != Boolean.FALSE) {
            Boolean v__17285__auto__17664;
            Boolean bl5 = temp__5457__auto__17665;
            temp__5457__auto__17665 = null;
            Boolean bl6 = v__17285__auto__17664 = bl5;
            v__17285__auto__17664 = null;
            iPersistentVector = Tuple.create((Object)const__26, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl6));
        } else {
            iPersistentVector = null;
        }
        objectArray[3] = iPersistentVector;
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector24, (Object)iPersistentVector23, (Object)iPersistentVector22, (Object)iPersistentVector21, (Object)iPersistentVector20, (Object)iPersistentVector19, (Object)iPersistentVector18, (Object)iPersistentVector17, (Object)iPersistentVector16, (Object)iPersistentVector15, (Object)iPersistentVector14, (Object)iPersistentVector13, (Object)iPersistentVector12, (Object)iPersistentVector11, (Object)iPersistentVector10, (Object)iPersistentVector9, (Object)iPersistentVector8, (Object)iPersistentVector7, (Object)iPersistentVector6, (Object)iPersistentVector5, objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17616.invokeStatic(object2);
    }
}

