package net.caze.roamingcontrol;

interface SharedPreferencesValues {
    String PACKAGE = "net.caze.roamingcontrol";
    //Identifiers for ListAdapter
    int NETWORK = 1001;
    int COUNTRY = 1002;
    int FORCE = 1003;

    //Values for string-array
    String NETWORK_VALUE = "Network";
    String COUNTRY_VALUE = "Country";

    //File names, tag and preference file name
    String TAG = "RoamingControl";
    String NETWORK_FILENAME = "NetworkList";
    String COUNTRY_FILENAME = "CountryList";
    String FORCE_FILENAME = "ForceList";
    String PREFNAME = "net.caze.roamingcontrol_pref";

    //Shared preferences
    String KEY_PREF_NATIONAL = "user_national";
    String KEY_PREF_FORCE = "user_forceroaming";
    String KEY_PREF_WHITELIST_MODE = "user_whitelist_mode";
    String KEY_PREF_ROAMING_TYPE = "user_roaming_type";
    String KEY_PREF_SAVED_ROAMING = "user_saved";
    String KEY_PREF_ADVANCED_FORCE = "user_advanced_force";
    String KEY_PREF_DUALSIM = "user_dualsim";
    String KEY_PREF_MATCH_NAME = "user_match_name";
    String KEY_PREF_HIDE_ICON = "user_hide";

    String DIALOG_ROAMING_SKIP = "dialog_roaming_skip";
    String BASE_MCC_LIST = "baseMCClist";

    //Sort orders for saved and force roaming
    String NETWORK_SORT_ORDER = "NETWORK_SORT_ORDER";
    String COUNTRY_SORT_ORDER = "COUNTRY_SORT_ORDER";
    String FORCE_SORT_ORDER = "FORCE_SORT_ORDER";

    //For dualsim
    String KEY_PREF_SIM_SELECTED = "user_sim_selected";
    String KEY_PREF_NATIONAL_SIM1 = "user_national_sim1";
    String KEY_PREF_NATIONAL_SIM2 = "user_national_sim2";
    String KEY_PREF_FORCE_SIM1 = "user_force_sim1";
    String KEY_PREF_FORCE_SIM2 = "user_force_sim2";

    String NETWORK_FILENAME_SIM1 = "NetworkList_SIM1";
    String NETWORK_FILENAME_SIM2 = "NetworkList_SIM2";
    String COUNTRY_FILENAME_SIM1 = "CountryList_SIM1";
    String COUNTRY_FILENAME_SIM2 = "CountryList_SIM2";
    String FORCE_FILENAME_SIM1 = "ForceList_SIM1";
    String FORCE_FILENAME_SIM2 = "ForceList_SIM2";

    //Old sharedpreferences
    String KEY_PREF_MIGRATIONE_DONE = "migration_done";
    String OLD_SAVED_NETWORKS = "saved_networks";
    String OLD_SAVED_COUNTRIES = "saved_countries";
    String OLD_FORCE_SAVED_NETWORKS = "force_saved_networks";
    String OLD_SAVED_NETWORKS_SIM1 = "saved_networks_sim1";
    String OLD_SAVED_NETWORKS_SIM2 = "saved_networks_sim2";
    String OLD_FORCE_SAVED_NETWORKS_SIM1 = "force_saved_networks_sim1";
    String OLD_FORCE_SAVED_NETWORKS_SIM2 = "force_saved_networks_sim2";
    String OLD_SHOW_MCC = "showMCC";
    String OLD_SHOW_MNC = "showMNC";
    String OLD_DUALSIM_POPUP = "dualsimPopup";

    String REALLY_OLD_SAVED_NETWORKS_XPOSED = "saved_networks_xposed";

}
