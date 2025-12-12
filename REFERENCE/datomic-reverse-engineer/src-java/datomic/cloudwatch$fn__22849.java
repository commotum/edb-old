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
 *  com.amazonaws.services.cloudwatch.model.MetricAlarm
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import java.util.Date;
import java.util.List;

public final class cloudwatch$fn__22849
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"namespace");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"dimensions");
    public static final Keyword const__6 = RT.keyword(null, (String)"metrics");
    public static final Keyword const__7 = RT.keyword(null, (String)"comparisonOperator");
    public static final Keyword const__8 = RT.keyword(null, (String)"statistic");
    public static final Keyword const__9 = RT.keyword(null, (String)"stateValue");
    public static final Keyword const__10 = RT.keyword(null, (String)"alarmName");
    public static final Keyword const__11 = RT.keyword(null, (String)"alarmArn");
    public static final Keyword const__12 = RT.keyword(null, (String)"alarmDescription");
    public static final Keyword const__13 = RT.keyword(null, (String)"alarmConfigurationUpdatedTimestamp");
    public static final Keyword const__14 = RT.keyword(null, (String)"actionsEnabled");
    public static final Keyword const__15 = RT.keyword(null, (String)"oKActions");
    public static final Keyword const__16 = RT.keyword(null, (String)"alarmActions");
    public static final Keyword const__17 = RT.keyword(null, (String)"insufficientDataActions");
    public static final Keyword const__18 = RT.keyword(null, (String)"stateReason");
    public static final Keyword const__19 = RT.keyword(null, (String)"stateReasonData");
    public static final Keyword const__20 = RT.keyword(null, (String)"stateUpdatedTimestamp");
    public static final Keyword const__21 = RT.keyword(null, (String)"extendedStatistic");
    public static final Keyword const__22 = RT.keyword(null, (String)"evaluationPeriods");
    public static final Keyword const__23 = RT.keyword(null, (String)"datapointsToAlarm");
    public static final Keyword const__24 = RT.keyword(null, (String)"threshold");
    public static final Keyword const__25 = RT.keyword(null, (String)"treatMissingData");
    public static final Keyword const__26 = RT.keyword(null, (String)"evaluateLowSampleCountPercentile");
    public static final Keyword const__27 = RT.keyword(null, (String)"thresholdMetricId");
    public static final Keyword const__28 = RT.keyword(null, (String)"evaluationState");
    public static final Keyword const__29 = RT.keyword(null, (String)"stateTransitionedTimestamp");
    public static final Keyword const__30 = RT.keyword(null, (String)"metricName");
    public static final Keyword const__31 = RT.keyword(null, (String)"period");
    public static final Keyword const__32 = RT.keyword(null, (String)"unit");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        String temp__5457__auto__22910;
        IPersistentVector iPersistentVector2;
        Integer temp__5457__auto__22908;
        IPersistentVector iPersistentVector3;
        String temp__5457__auto__22906;
        IPersistentVector iPersistentVector4;
        Date temp__5457__auto__22904;
        IPersistentVector iPersistentVector5;
        String temp__5457__auto__22902;
        IPersistentVector iPersistentVector6;
        String temp__5457__auto__22900;
        IPersistentVector iPersistentVector7;
        String temp__5457__auto__22898;
        IPersistentVector iPersistentVector8;
        String temp__5457__auto__22896;
        IPersistentVector iPersistentVector9;
        Double temp__5457__auto__22894;
        IPersistentVector iPersistentVector10;
        Integer temp__5457__auto__22892;
        IPersistentVector iPersistentVector11;
        Integer temp__5457__auto__22890;
        IPersistentVector iPersistentVector12;
        String temp__5457__auto__22888;
        IPersistentVector iPersistentVector13;
        Date temp__5457__auto__22886;
        IPersistentVector iPersistentVector14;
        String temp__5457__auto__22884;
        IPersistentVector iPersistentVector15;
        String temp__5457__auto__22882;
        IPersistentVector iPersistentVector16;
        List temp__5457__auto__22880;
        IPersistentVector iPersistentVector17;
        List temp__5457__auto__22878;
        IPersistentVector iPersistentVector18;
        List temp__5457__auto__22876;
        IPersistentVector iPersistentVector19;
        Boolean temp__5457__auto__22874;
        IPersistentVector iPersistentVector20;
        Boolean temp__5457__auto__22872;
        IPersistentVector iPersistentVector21;
        Date temp__5457__auto__22870;
        IPersistentVector iPersistentVector22;
        String temp__5457__auto__22868;
        IPersistentVector iPersistentVector23;
        String temp__5457__auto__22866;
        IPersistentVector iPersistentVector24;
        String temp__5457__auto__22864;
        IPersistentVector iPersistentVector25;
        String temp__5457__auto__22862;
        IPersistentVector iPersistentVector26;
        String temp__5457__auto__22860;
        IPersistentVector iPersistentVector27;
        String temp__5457__auto__22858;
        IPersistentVector iPersistentVector28;
        List temp__5457__auto__22856;
        IPersistentVector iPersistentVector29;
        List temp__5457__auto__22854;
        IPersistentVector iPersistentVector30;
        String temp__5457__auto__22852;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        String string = temp__5457__auto__22852 = ((MetricAlarm)o).getNamespace();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__22851;
            String string2 = temp__5457__auto__22852;
            temp__5457__auto__22852 = null;
            String string3 = v__17285__auto__22851 = string2;
            v__17285__auto__22851 = null;
            iPersistentVector30 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector30 = null;
        }
        List list = temp__5457__auto__22854 = ((MetricAlarm)o).getDimensions();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__22853;
            List list2 = temp__5457__auto__22854;
            temp__5457__auto__22854 = null;
            List list3 = v__17285__auto__22853 = list2;
            v__17285__auto__22853 = null;
            iPersistentVector29 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector29 = null;
        }
        List list4 = temp__5457__auto__22856 = ((MetricAlarm)o).getMetrics();
        if (list4 != null && list4 != Boolean.FALSE) {
            List v__17285__auto__22855;
            List list5 = temp__5457__auto__22856;
            temp__5457__auto__22856 = null;
            List list6 = v__17285__auto__22855 = list5;
            v__17285__auto__22855 = null;
            iPersistentVector28 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list6));
        } else {
            iPersistentVector28 = null;
        }
        String string4 = temp__5457__auto__22858 = ((MetricAlarm)o).getComparisonOperator();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__22857;
            String string5 = temp__5457__auto__22858;
            temp__5457__auto__22858 = null;
            String string6 = v__17285__auto__22857 = string5;
            v__17285__auto__22857 = null;
            iPersistentVector27 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector27 = null;
        }
        String string7 = temp__5457__auto__22860 = ((MetricAlarm)o).getStatistic();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__22859;
            String string8 = temp__5457__auto__22860;
            temp__5457__auto__22860 = null;
            String string9 = v__17285__auto__22859 = string8;
            v__17285__auto__22859 = null;
            iPersistentVector26 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector26 = null;
        }
        String string10 = temp__5457__auto__22862 = ((MetricAlarm)o).getStateValue();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__22861;
            String string11 = temp__5457__auto__22862;
            temp__5457__auto__22862 = null;
            String string12 = v__17285__auto__22861 = string11;
            v__17285__auto__22861 = null;
            iPersistentVector25 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector25 = null;
        }
        String string13 = temp__5457__auto__22864 = ((MetricAlarm)o).getAlarmName();
        if (string13 != null && string13 != Boolean.FALSE) {
            String v__17285__auto__22863;
            String string14 = temp__5457__auto__22864;
            temp__5457__auto__22864 = null;
            String string15 = v__17285__auto__22863 = string14;
            v__17285__auto__22863 = null;
            iPersistentVector24 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string15));
        } else {
            iPersistentVector24 = null;
        }
        String string16 = temp__5457__auto__22866 = ((MetricAlarm)o).getAlarmArn();
        if (string16 != null && string16 != Boolean.FALSE) {
            String v__17285__auto__22865;
            String string17 = temp__5457__auto__22866;
            temp__5457__auto__22866 = null;
            String string18 = v__17285__auto__22865 = string17;
            v__17285__auto__22865 = null;
            iPersistentVector23 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string18));
        } else {
            iPersistentVector23 = null;
        }
        String string19 = temp__5457__auto__22868 = ((MetricAlarm)o).getAlarmDescription();
        if (string19 != null && string19 != Boolean.FALSE) {
            String v__17285__auto__22867;
            String string20 = temp__5457__auto__22868;
            temp__5457__auto__22868 = null;
            String string21 = v__17285__auto__22867 = string20;
            v__17285__auto__22867 = null;
            iPersistentVector22 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string21));
        } else {
            iPersistentVector22 = null;
        }
        Date date = temp__5457__auto__22870 = ((MetricAlarm)o).getAlarmConfigurationUpdatedTimestamp();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__22869;
            Date date2 = temp__5457__auto__22870;
            temp__5457__auto__22870 = null;
            Date date3 = v__17285__auto__22869 = date2;
            v__17285__auto__22869 = null;
            iPersistentVector21 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector21 = null;
        }
        Boolean bl = temp__5457__auto__22872 = ((MetricAlarm)o).getActionsEnabled();
        if (bl != null && bl != Boolean.FALSE) {
            Boolean v__17285__auto__22871;
            Boolean bl2 = temp__5457__auto__22872;
            temp__5457__auto__22872 = null;
            Boolean bl3 = v__17285__auto__22871 = bl2;
            v__17285__auto__22871 = null;
            iPersistentVector20 = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl3));
        } else {
            iPersistentVector20 = null;
        }
        Boolean bl4 = temp__5457__auto__22874 = ((MetricAlarm)o).isActionsEnabled();
        if (bl4 != null && bl4 != Boolean.FALSE) {
            Boolean v__17285__auto__22873;
            Boolean bl5 = temp__5457__auto__22874;
            temp__5457__auto__22874 = null;
            Boolean bl6 = v__17285__auto__22873 = bl5;
            v__17285__auto__22873 = null;
            iPersistentVector19 = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl6));
        } else {
            iPersistentVector19 = null;
        }
        List list7 = temp__5457__auto__22876 = ((MetricAlarm)o).getOKActions();
        if (list7 != null && list7 != Boolean.FALSE) {
            List v__17285__auto__22875;
            List list8 = temp__5457__auto__22876;
            temp__5457__auto__22876 = null;
            List list9 = v__17285__auto__22875 = list8;
            v__17285__auto__22875 = null;
            iPersistentVector18 = Tuple.create((Object)const__15, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list9));
        } else {
            iPersistentVector18 = null;
        }
        List list10 = temp__5457__auto__22878 = ((MetricAlarm)o).getAlarmActions();
        if (list10 != null && list10 != Boolean.FALSE) {
            List v__17285__auto__22877;
            List list11 = temp__5457__auto__22878;
            temp__5457__auto__22878 = null;
            List list12 = v__17285__auto__22877 = list11;
            v__17285__auto__22877 = null;
            iPersistentVector17 = Tuple.create((Object)const__16, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list12));
        } else {
            iPersistentVector17 = null;
        }
        List list13 = temp__5457__auto__22880 = ((MetricAlarm)o).getInsufficientDataActions();
        if (list13 != null && list13 != Boolean.FALSE) {
            List v__17285__auto__22879;
            List list14 = temp__5457__auto__22880;
            temp__5457__auto__22880 = null;
            List list15 = v__17285__auto__22879 = list14;
            v__17285__auto__22879 = null;
            iPersistentVector16 = Tuple.create((Object)const__17, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list15));
        } else {
            iPersistentVector16 = null;
        }
        String string22 = temp__5457__auto__22882 = ((MetricAlarm)o).getStateReason();
        if (string22 != null && string22 != Boolean.FALSE) {
            String v__17285__auto__22881;
            String string23 = temp__5457__auto__22882;
            temp__5457__auto__22882 = null;
            String string24 = v__17285__auto__22881 = string23;
            v__17285__auto__22881 = null;
            iPersistentVector15 = Tuple.create((Object)const__18, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string24));
        } else {
            iPersistentVector15 = null;
        }
        String string25 = temp__5457__auto__22884 = ((MetricAlarm)o).getStateReasonData();
        if (string25 != null && string25 != Boolean.FALSE) {
            String v__17285__auto__22883;
            String string26 = temp__5457__auto__22884;
            temp__5457__auto__22884 = null;
            String string27 = v__17285__auto__22883 = string26;
            v__17285__auto__22883 = null;
            iPersistentVector14 = Tuple.create((Object)const__19, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string27));
        } else {
            iPersistentVector14 = null;
        }
        Date date4 = temp__5457__auto__22886 = ((MetricAlarm)o).getStateUpdatedTimestamp();
        if (date4 != null && date4 != Boolean.FALSE) {
            Date v__17285__auto__22885;
            Date date5 = temp__5457__auto__22886;
            temp__5457__auto__22886 = null;
            Date date6 = v__17285__auto__22885 = date5;
            v__17285__auto__22885 = null;
            iPersistentVector13 = Tuple.create((Object)const__20, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date6));
        } else {
            iPersistentVector13 = null;
        }
        String string28 = temp__5457__auto__22888 = ((MetricAlarm)o).getExtendedStatistic();
        if (string28 != null && string28 != Boolean.FALSE) {
            String v__17285__auto__22887;
            String string29 = temp__5457__auto__22888;
            temp__5457__auto__22888 = null;
            String string30 = v__17285__auto__22887 = string29;
            v__17285__auto__22887 = null;
            iPersistentVector12 = Tuple.create((Object)const__21, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string30));
        } else {
            iPersistentVector12 = null;
        }
        Integer n = temp__5457__auto__22890 = ((MetricAlarm)o).getEvaluationPeriods();
        if (n != null && n != Boolean.FALSE) {
            Integer v__17285__auto__22889;
            Integer n2 = temp__5457__auto__22890;
            temp__5457__auto__22890 = null;
            Integer n3 = v__17285__auto__22889 = n2;
            v__17285__auto__22889 = null;
            iPersistentVector11 = Tuple.create((Object)const__22, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n3));
        } else {
            iPersistentVector11 = null;
        }
        Object[] objectArray = new Object[10];
        Integer n4 = temp__5457__auto__22892 = ((MetricAlarm)o).getDatapointsToAlarm();
        if (n4 != null && n4 != Boolean.FALSE) {
            Integer v__17285__auto__22891;
            Integer n5 = temp__5457__auto__22892;
            temp__5457__auto__22892 = null;
            Integer n6 = v__17285__auto__22891 = n5;
            v__17285__auto__22891 = null;
            iPersistentVector10 = Tuple.create((Object)const__23, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n6));
        } else {
            iPersistentVector10 = null;
        }
        objectArray[0] = iPersistentVector10;
        Double d = temp__5457__auto__22894 = ((MetricAlarm)o).getThreshold();
        if (d != null && d != Boolean.FALSE) {
            Double v__17285__auto__22893;
            Double d2 = temp__5457__auto__22894;
            temp__5457__auto__22894 = null;
            Double d3 = v__17285__auto__22893 = d2;
            v__17285__auto__22893 = null;
            iPersistentVector9 = Tuple.create((Object)const__24, (Object)((IFn)const__4.getRawRoot()).invoke((Object)d3));
        } else {
            iPersistentVector9 = null;
        }
        objectArray[1] = iPersistentVector9;
        String string31 = temp__5457__auto__22896 = ((MetricAlarm)o).getTreatMissingData();
        if (string31 != null && string31 != Boolean.FALSE) {
            String v__17285__auto__22895;
            String string32 = temp__5457__auto__22896;
            temp__5457__auto__22896 = null;
            String string33 = v__17285__auto__22895 = string32;
            v__17285__auto__22895 = null;
            iPersistentVector8 = Tuple.create((Object)const__25, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string33));
        } else {
            iPersistentVector8 = null;
        }
        objectArray[2] = iPersistentVector8;
        String string34 = temp__5457__auto__22898 = ((MetricAlarm)o).getEvaluateLowSampleCountPercentile();
        if (string34 != null && string34 != Boolean.FALSE) {
            String v__17285__auto__22897;
            String string35 = temp__5457__auto__22898;
            temp__5457__auto__22898 = null;
            String string36 = v__17285__auto__22897 = string35;
            v__17285__auto__22897 = null;
            iPersistentVector7 = Tuple.create((Object)const__26, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string36));
        } else {
            iPersistentVector7 = null;
        }
        objectArray[3] = iPersistentVector7;
        String string37 = temp__5457__auto__22900 = ((MetricAlarm)o).getThresholdMetricId();
        if (string37 != null && string37 != Boolean.FALSE) {
            String v__17285__auto__22899;
            String string38 = temp__5457__auto__22900;
            temp__5457__auto__22900 = null;
            String string39 = v__17285__auto__22899 = string38;
            v__17285__auto__22899 = null;
            iPersistentVector6 = Tuple.create((Object)const__27, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string39));
        } else {
            iPersistentVector6 = null;
        }
        objectArray[4] = iPersistentVector6;
        String string40 = temp__5457__auto__22902 = ((MetricAlarm)o).getEvaluationState();
        if (string40 != null && string40 != Boolean.FALSE) {
            String v__17285__auto__22901;
            String string41 = temp__5457__auto__22902;
            temp__5457__auto__22902 = null;
            String string42 = v__17285__auto__22901 = string41;
            v__17285__auto__22901 = null;
            iPersistentVector5 = Tuple.create((Object)const__28, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string42));
        } else {
            iPersistentVector5 = null;
        }
        objectArray[5] = iPersistentVector5;
        Date date7 = temp__5457__auto__22904 = ((MetricAlarm)o).getStateTransitionedTimestamp();
        if (date7 != null && date7 != Boolean.FALSE) {
            Date v__17285__auto__22903;
            Date date8 = temp__5457__auto__22904;
            temp__5457__auto__22904 = null;
            Date date9 = v__17285__auto__22903 = date8;
            v__17285__auto__22903 = null;
            iPersistentVector4 = Tuple.create((Object)const__29, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date9));
        } else {
            iPersistentVector4 = null;
        }
        objectArray[6] = iPersistentVector4;
        String string43 = temp__5457__auto__22906 = ((MetricAlarm)o).getMetricName();
        if (string43 != null && string43 != Boolean.FALSE) {
            String v__17285__auto__22905;
            String string44 = temp__5457__auto__22906;
            temp__5457__auto__22906 = null;
            String string45 = v__17285__auto__22905 = string44;
            v__17285__auto__22905 = null;
            iPersistentVector3 = Tuple.create((Object)const__30, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string45));
        } else {
            iPersistentVector3 = null;
        }
        objectArray[7] = iPersistentVector3;
        Integer n7 = temp__5457__auto__22908 = ((MetricAlarm)o).getPeriod();
        if (n7 != null && n7 != Boolean.FALSE) {
            Integer v__17285__auto__22907;
            Integer n8 = temp__5457__auto__22908;
            temp__5457__auto__22908 = null;
            Integer n9 = v__17285__auto__22907 = n8;
            v__17285__auto__22907 = null;
            iPersistentVector2 = Tuple.create((Object)const__31, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n9));
        } else {
            iPersistentVector2 = null;
        }
        objectArray[8] = iPersistentVector2;
        Object object2 = o;
        o = null;
        String string46 = temp__5457__auto__22910 = ((MetricAlarm)object2).getUnit();
        if (string46 != null && string46 != Boolean.FALSE) {
            String v__17285__auto__22909;
            String string47 = temp__5457__auto__22910;
            temp__5457__auto__22910 = null;
            String string48 = v__17285__auto__22909 = string47;
            v__17285__auto__22909 = null;
            iPersistentVector = Tuple.create((Object)const__32, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string48));
        } else {
            iPersistentVector = null;
        }
        objectArray[9] = iPersistentVector;
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector30, (Object)iPersistentVector29, (Object)iPersistentVector28, (Object)iPersistentVector27, (Object)iPersistentVector26, (Object)iPersistentVector25, (Object)iPersistentVector24, (Object)iPersistentVector23, (Object)iPersistentVector22, (Object)iPersistentVector21, (Object)iPersistentVector20, (Object)iPersistentVector19, (Object)iPersistentVector18, (Object)iPersistentVector17, (Object)iPersistentVector16, (Object)iPersistentVector15, (Object)iPersistentVector14, (Object)iPersistentVector13, (Object)iPersistentVector12, (Object)iPersistentVector11, objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cloudwatch$fn__22849.invokeStatic(object2);
    }
}

