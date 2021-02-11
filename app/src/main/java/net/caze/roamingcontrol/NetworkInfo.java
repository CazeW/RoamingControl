package net.caze.roamingcontrol;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;


class NetworkInfo implements SharedPreferencesValues {
    private static NetworkInfo instance = null;
    private static Resources resources;
    private static TelephonyManager tel;

    private static LinkedHashMap<String, String> countryList;
    private static TreeMap<String, String> baseCountryCodes;


    private NetworkInfo() {}

    static NetworkInfo getInstance() {
        return instance;
    }

    static void getInstance(Application app) {
        if (instance == null) {
            instance = new NetworkInfo();
            countryList = new LinkedHashMap<>();
            baseCountryCodes = new TreeMap<>();
            resources = app.getResources();
            tel = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
            instance.updateCountryList();
        }
    }

    String getOperatorCountry(String mcc) {
        String country = countryList.get(baseMcc(mcc));
        if (country != null)
            return country;
        else
            return resources.getString(R.string.unknown);
    }

    Network getCurrentNetwork(Context context) {
        String networkName = "";
        String networkCountry, networkMCC, networkMNC;

        if (MainActivity.isDualSIM() && android.os.Build.VERSION.SDK_INT >= 22) {
            TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator;
            int phone = MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0);
            networkOperator = tel.getNetworkOperatorForPhone(phone);

            SubscriptionManager subMan = SubscriptionManager.from(context);
            List<SubscriptionInfo> subs = subMan.getActiveSubscriptionInfoList();

            try {
                networkName = subs.get(phone).getCarrierName().toString();
                //networkName = TelephonyManager.getTelephonyProperty(phone, TelephonyProperties.PROPERTY_OPERATOR_ALPHA, "");
                networkCountry = getOperatorCountry(networkOperator.substring(0, 3));
                networkMCC = networkOperator.substring(0, 3);
                networkMNC = networkOperator.substring(3);
            } catch (Exception e) {
                networkName = resources.getString(R.string.unknown);
                networkCountry = "";
                networkMCC = "---";
                networkMNC = "---";
            }
        } else {
            String networkOperator = tel.getNetworkOperator();
            if (!networkOperator.isEmpty()) {
                networkName = tel.getNetworkOperatorName();
                networkCountry = getOperatorCountry(networkOperator.substring(0, 3));
                networkMCC = networkOperator.substring(0, 3);
                networkMNC = networkOperator.substring(3);
            } else {
                networkName = resources.getString(R.string.unknown);
                networkCountry = "";
                networkMCC = "---";
                networkMNC = "---";
            }
        }
        return new Network(networkName, networkCountry, networkMCC, networkMNC);
    }

    String getSimNetwork(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int phone = MainActivity.sharedPref.getInt(KEY_PREF_SIM_SELECTED, 0);
        return tel.getSimOperatorNumericForPhone(phone);
    }

    /*String searchMCC(String country) {
        for(LinkedHashMap.Entry entry: countryList.entrySet()) {
            if(country.equals(entry.getValue().toString())){
                return entry.getKey().toString();
            }
        }
        return resources.getString(R.string.unknown);
    }*/

    String baseMcc(String mcc) {
        String baseMcc = baseCountryCodes.get(mcc);
        if (baseMcc != null)
            return baseMcc;
        else
            return mcc;
    }

    LinkedHashMap<String, String> getCountryList() {
        return countryList;
    }

    String searchKey(String country) {
        for(LinkedHashMap.Entry entry: countryList.entrySet()) {
            if(country.equals(entry.getValue().toString())){
                return entry.getKey().toString();
            }
        }
        return resources.getString(R.string.unknown);
    }

    //Country list
    private void updateCountryList() {
        countryList.put("289", resources.getString(R.string.abkhazia));
        countryList.put("412", resources.getString(R.string.afghanistan));
        countryList.put("276", resources.getString(R.string.albania));
        countryList.put("603", resources.getString(R.string.algeria));
        countryList.put("544", resources.getString(R.string.american_samoa));
        countryList.put("213", resources.getString(R.string.andorra));
        countryList.put("631", resources.getString(R.string.angola));
        countryList.put("365", resources.getString(R.string.anguilla));
        countryList.put("344", resources.getString(R.string.antigua_and_barbuda));
        countryList.put("722", resources.getString(R.string.argentina));
        countryList.put("283", resources.getString(R.string.armenia));
        countryList.put("363", resources.getString(R.string.aruba));
        countryList.put("505", resources.getString(R.string.australia));
        countryList.put("232", resources.getString(R.string.austria));
        countryList.put("400", resources.getString(R.string.azerbaijan));
        countryList.put("364", resources.getString(R.string.bahamas));
        countryList.put("426", resources.getString(R.string.bahrain));
        countryList.put("470", resources.getString(R.string.bangladesh));
        countryList.put("342", resources.getString(R.string.barbados));
        countryList.put("257", resources.getString(R.string.belarus));
        countryList.put("206", resources.getString(R.string.belgium));
        countryList.put("702", resources.getString(R.string.belize));
        countryList.put("616", resources.getString(R.string.benin));
        countryList.put("350", resources.getString(R.string.bermuda));
        countryList.put("402", resources.getString(R.string.bhutan));
        countryList.put("736", resources.getString(R.string.bolivia));
        countryList.put("218", resources.getString(R.string.bosnia_and_herzegovina));
        countryList.put("652", resources.getString(R.string.botswana));
        countryList.put("724", resources.getString(R.string.brazil));
        countryList.put("348", resources.getString(R.string.british_virgin_islands));
        countryList.put("528", resources.getString(R.string.brunei));
        countryList.put("284", resources.getString(R.string.bulgaria));
        countryList.put("613", resources.getString(R.string.burkina_faso));
        countryList.put("414", resources.getString(R.string.burma));
        countryList.put("642", resources.getString(R.string.burundi));
        countryList.put("456", resources.getString(R.string.cambodia));
        countryList.put("624", resources.getString(R.string.cameroon));
        countryList.put("302", resources.getString(R.string.canada));
        countryList.put("625", resources.getString(R.string.cape_verde));
        countryList.put("346", resources.getString(R.string.cayman_islands));
        countryList.put("623", resources.getString(R.string.central_african_republic));
        countryList.put("622", resources.getString(R.string.chad));
        countryList.put("730", resources.getString(R.string.chile));
        countryList.put("460", resources.getString(R.string.china));
        countryList.put("732", resources.getString(R.string.colombia));
        countryList.put("654", resources.getString(R.string.comoros));
        countryList.put("630", resources.getString(R.string.congo_democratic_republic));
        countryList.put("629", resources.getString(R.string.congo));
        countryList.put("548", resources.getString(R.string.cook_islands));
        countryList.put("712", resources.getString(R.string.costa_rica));
        countryList.put("219", resources.getString(R.string.croatia));
        countryList.put("368", resources.getString(R.string.cuba));
        countryList.put("362", resources.getString(R.string.curacao));
        countryList.put("280", resources.getString(R.string.cyprus));
        countryList.put("230", resources.getString(R.string.czech_republic));
        countryList.put("238", resources.getString(R.string.denmark));
        countryList.put("638", resources.getString(R.string.djibouti));
        countryList.put("366", resources.getString(R.string.dominica));
        countryList.put("370", resources.getString(R.string.dominican_republic));
        countryList.put("740", resources.getString(R.string.ecuador));
        countryList.put("602", resources.getString(R.string.egypt));
        countryList.put("706", resources.getString(R.string.el_salvador));
        countryList.put("627", resources.getString(R.string.equatorial_guinea));
        countryList.put("657", resources.getString(R.string.eritrea));
        countryList.put("248", resources.getString(R.string.estonia));
        countryList.put("636", resources.getString(R.string.ethiopia));
        countryList.put("750", resources.getString(R.string.falkland_islands));
        countryList.put("288", resources.getString(R.string.faroe_islands));
        countryList.put("542", resources.getString(R.string.fiji));
        countryList.put("244", resources.getString(R.string.finland));
        countryList.put("208", resources.getString(R.string.france));
        countryList.put("340", resources.getString(R.string.french_antilles));
        countryList.put("547", resources.getString(R.string.french_polynesia));
        countryList.put("628", resources.getString(R.string.gabon));
        countryList.put("607", resources.getString(R.string.gambia));
        countryList.put("282", resources.getString(R.string.georgia));
        countryList.put("262", resources.getString(R.string.germany));
        countryList.put("620", resources.getString(R.string.ghana));
        countryList.put("266", resources.getString(R.string.gibraltar));
        countryList.put("202", resources.getString(R.string.greece));
        countryList.put("290", resources.getString(R.string.greenland));
        countryList.put("352", resources.getString(R.string.grenada));
        countryList.put("704", resources.getString(R.string.guatemala));
        countryList.put("611", resources.getString(R.string.guinea));
        countryList.put("632", resources.getString(R.string.guinea_bissau));
        countryList.put("738", resources.getString(R.string.guyana));
        countryList.put("372", resources.getString(R.string.haiti));
        countryList.put("708", resources.getString(R.string.honduras));
        countryList.put("454", resources.getString(R.string.hong_kong));
        countryList.put("216", resources.getString(R.string.hungary));
        countryList.put("274", resources.getString(R.string.iceland));
        countryList.put("404", resources.getString(R.string.india));
        //countryList.put("405", resources.getString(R.string.india));
        countryList.put("510", resources.getString(R.string.indonesia));
        countryList.put("432", resources.getString(R.string.iran));
        countryList.put("418", resources.getString(R.string.iraq));
        countryList.put("272", resources.getString(R.string.ireland));
        countryList.put("425", resources.getString(R.string.israel));
        countryList.put("222", resources.getString(R.string.italy));
        countryList.put("612", resources.getString(R.string.ivory_coast));
        countryList.put("338", resources.getString(R.string.jamaica));
        countryList.put("440", resources.getString(R.string.japan));
        //countryList.put("441", resources.getString(R.string.japan));
        countryList.put("416", resources.getString(R.string.jordan));
        countryList.put("401", resources.getString(R.string.kazakhstan));
        countryList.put("639", resources.getString(R.string.kenya));
        countryList.put("545", resources.getString(R.string.kiribati));
        countryList.put("419", resources.getString(R.string.kuwait));
        countryList.put("437", resources.getString(R.string.kyrgyzstan));
        countryList.put("457", resources.getString(R.string.laos));
        countryList.put("247", resources.getString(R.string.latvia));
        countryList.put("415", resources.getString(R.string.lebanon));
        countryList.put("651", resources.getString(R.string.lesotho));
        countryList.put("618", resources.getString(R.string.liberia));
        countryList.put("606", resources.getString(R.string.libya));
        countryList.put("295", resources.getString(R.string.liechtenstein));
        countryList.put("246", resources.getString(R.string.lithuania));
        countryList.put("270", resources.getString(R.string.luxembourg));
        countryList.put("455", resources.getString(R.string.macau_china));
        countryList.put("294", resources.getString(R.string.macedonia));
        countryList.put("646", resources.getString(R.string.madagascar));
        countryList.put("650", resources.getString(R.string.malawi));
        countryList.put("502", resources.getString(R.string.malaysia));
        countryList.put("472", resources.getString(R.string.maldives));
        countryList.put("610", resources.getString(R.string.mali));
        countryList.put("278", resources.getString(R.string.malta));
        countryList.put("609", resources.getString(R.string.mauritania));
        countryList.put("617", resources.getString(R.string.mauritius));
        countryList.put("334", resources.getString(R.string.mexico));
        countryList.put("550", resources.getString(R.string.micronesia));
        countryList.put("259", resources.getString(R.string.moldova));
        countryList.put("212", resources.getString(R.string.monaco));
        countryList.put("428", resources.getString(R.string.mongolia));
        countryList.put("297", resources.getString(R.string.montenegro));
        countryList.put("354", resources.getString(R.string.montserrat));
        countryList.put("604", resources.getString(R.string.morocco));
        countryList.put("643", resources.getString(R.string.mozambique));
        countryList.put("649", resources.getString(R.string.namibia));
        countryList.put("429", resources.getString(R.string.nepal));
        countryList.put("204", resources.getString(R.string.netherlands));
        countryList.put("546", resources.getString(R.string.new_caledonia));
        countryList.put("530", resources.getString(R.string.new_zealand));
        countryList.put("710", resources.getString(R.string.nicaragua));
        countryList.put("614", resources.getString(R.string.niger));
        countryList.put("621", resources.getString(R.string.nigeria));
        countryList.put("555", resources.getString(R.string.niue));
        countryList.put("467", resources.getString(R.string.north_korea));
        countryList.put("242", resources.getString(R.string.norway));
        countryList.put("422", resources.getString(R.string.oman));
        countryList.put("410", resources.getString(R.string.pakistan));
        countryList.put("552", resources.getString(R.string.palau));
        countryList.put("714", resources.getString(R.string.panama));
        countryList.put("537", resources.getString(R.string.papua_new_guinea));
        countryList.put("744", resources.getString(R.string.paraguay));
        countryList.put("716", resources.getString(R.string.peru));
        countryList.put("515", resources.getString(R.string.philippines));
        countryList.put("260", resources.getString(R.string.poland));
        countryList.put("268", resources.getString(R.string.portugal));
        countryList.put("330", resources.getString(R.string.puerto_rico));
        countryList.put("427", resources.getString(R.string.qatar));
        countryList.put("647", resources.getString(R.string.reunion));
        countryList.put("226", resources.getString(R.string.romania));
        countryList.put("250", resources.getString(R.string.russia));
        countryList.put("635", resources.getString(R.string.rwanda));
        countryList.put("356", resources.getString(R.string.saint_kitts_and_nevis));
        countryList.put("358", resources.getString(R.string.saint_lucia));
        countryList.put("549", resources.getString(R.string.samoa));
        countryList.put("292", resources.getString(R.string.san_marino));
        countryList.put("626", resources.getString(R.string.sao_tome_and_principe));
        countryList.put("901", resources.getString(R.string.satellite_networks));
        countryList.put("420", resources.getString(R.string.saudi_arabia));
        countryList.put("608", resources.getString(R.string.senegal));
        countryList.put("220", resources.getString(R.string.serbia));
        countryList.put("633", resources.getString(R.string.seychelles));
        countryList.put("619", resources.getString(R.string.sierra_leone));
        countryList.put("525", resources.getString(R.string.singapore));
        countryList.put("231", resources.getString(R.string.slovakia));
        countryList.put("293", resources.getString(R.string.slovenia));
        countryList.put("540", resources.getString(R.string.solomon_islands));
        countryList.put("637", resources.getString(R.string.somalia));
        countryList.put("655", resources.getString(R.string.south_africa));
        countryList.put("450", resources.getString(R.string.south_korea));
        countryList.put("659", resources.getString(R.string.south_sudan));
        countryList.put("214", resources.getString(R.string.spain));
        countryList.put("413", resources.getString(R.string.sri_lanka));
        countryList.put("308", resources.getString(R.string.st_pierre_and_miquelon));
        countryList.put("360", resources.getString(R.string.st_vincent_and_grenadines));
        countryList.put("634", resources.getString(R.string.sudan));
        countryList.put("746", resources.getString(R.string.suriname));
        countryList.put("653", resources.getString(R.string.swaziland));
        countryList.put("240", resources.getString(R.string.sweden));
        countryList.put("228", resources.getString(R.string.switzerland));
        countryList.put("417", resources.getString(R.string.syrian_arab_republic));
        countryList.put("466", resources.getString(R.string.taiwan));
        countryList.put("436", resources.getString(R.string.tajikistan));
        countryList.put("640", resources.getString(R.string.tanzania));
        countryList.put("520", resources.getString(R.string.thailand));
        countryList.put("514", resources.getString(R.string.timor_leste));
        countryList.put("615", resources.getString(R.string.togo));
        countryList.put("539", resources.getString(R.string.tonga));
        countryList.put("374", resources.getString(R.string.trinidad_and_tobago));
        countryList.put("605", resources.getString(R.string.tunisia));
        countryList.put("286", resources.getString(R.string.turkey));
        countryList.put("438", resources.getString(R.string.turkmenistan));
        countryList.put("376", resources.getString(R.string.turks_and_caicos_islands));
        countryList.put("553", resources.getString(R.string.tuvalu));
        countryList.put("641", resources.getString(R.string.uganda));
        countryList.put("255", resources.getString(R.string.ukraine));
        countryList.put("424", resources.getString(R.string.united_arab_emirates));
        //countryList.put("430", resources.getString(R.string.united_arab_emirates));
        //countryList.put("431", resources.getString(R.string.united_arab_emirates));
        countryList.put("234", resources.getString(R.string.united_kingdom));
        //countryList.put("235", resources.getString(R.string.united_kingdom));
        countryList.put("310", resources.getString(R.string.united_states));
        //countryList.put("311", resources.getString(R.string.united_states));
        //countryList.put("312", resources.getString(R.string.united_states));
        //countryList.put("316", resources.getString(R.string.united_states));
        countryList.put("748", resources.getString(R.string.uruguay));
        countryList.put("434", resources.getString(R.string.uzbekistan));
        countryList.put("541", resources.getString(R.string.vanuatu));
        countryList.put("734", resources.getString(R.string.venezuela));
        countryList.put("452", resources.getString(R.string.vietnam));
        countryList.put("421", resources.getString(R.string.yemen));
        countryList.put("645", resources.getString(R.string.zambia));
        countryList.put("648", resources.getString(R.string.zimbabwe));

        //Countries with several MCC
        baseCountryCodes.put("405", "404"); // India
        baseCountryCodes.put("441", "440"); // Japan
        baseCountryCodes.put("430", "424"); // United Arab Emirates
        baseCountryCodes.put("431", "424"); // United Arab Emirates
        baseCountryCodes.put("235", "234"); // United Kingdom
        baseCountryCodes.put("311", "310"); // USA
        baseCountryCodes.put("312", "310"); // USA
        baseCountryCodes.put("316", "310"); // USA

        Set<String> new_baseMCClist = new HashSet<>();
        String mcclist = "";
        String value = "";
        for(TreeMap.Entry entry: baseCountryCodes.entrySet()) {
            if (!value.equals(entry.getValue().toString())) {
                if (!value.isEmpty()) {
                    new_baseMCClist.add(mcclist);
                }
                value = entry.getValue().toString();
                mcclist = value + "=";
            }
            mcclist += entry.getKey().toString() + ";";

        }
        Set<String> old_baseMCClist = new HashSet<>(MainActivity.sharedPref.getStringSet(BASE_MCC_LIST, new HashSet<String>()));
        if (!new_baseMCClist.equals(old_baseMCClist)) {
            MainActivity.sharedPref.edit().remove(BASE_MCC_LIST).apply();
            MainActivity.sharedPref.edit().putStringSet(BASE_MCC_LIST, new_baseMCClist).apply();
        }
    }

}
