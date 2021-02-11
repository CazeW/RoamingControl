package net.caze.roamingcontrol;


import java.io.Serializable;
import java.util.Comparator;

class Network implements Serializable {
    private String name, country, mcc, mnc, country_text, mcc_text, mnc_text, network;

    public Network() {}

    public Network(String name, String country, String mcc, String mnc) {
        this.name = name;
        this.country = country;
        this.mcc = mcc;
        this.mnc = mnc;
        this.country_text = " (" + country + ")";
        this.mcc_text = "MCC: " + mcc + " ";
        this.mnc_text = "(MNC: " + mnc + ")";
        this.network = this.mcc + this.mnc;
    }

    String getName() {
        return name;
    }

    String getCountry() {
        return country;
    }
    String getCountry_text() {
        return country_text;
    }
    String getMcc() {
        return mcc;
    }
    String getMcc_text() {
        return mcc_text;
    }
    String getMnc() {
        return mnc;
    }
    String getMnc_text() {
        return mnc_text;
    }
    String getNetwork() {return network;}

    static Comparator<Network> nameComparator = new Comparator<Network>() {
        @Override
        public int compare(Network network1, Network network2) {
            int nameComp = network1.getName().toLowerCase().compareTo(network2.getName().toLowerCase());
            if (nameComp != 0)
                return nameComp;

            int countryComp = network1.getCountry().compareTo(network2.getCountry());
            if (countryComp != 0)
                return countryComp;

            return network1.getMnc().compareTo(network2.getMnc());
        }
    };
    static Comparator<Network> countryComparator = new Comparator<Network>() {
        @Override
        public int compare(Network network1, Network network2) {
            int countryComp = network1.getCountry().compareTo(network2.getCountry());
            if (countryComp != 0)
                return countryComp;

            int nameComp = network1.getName().toLowerCase().compareTo(network2.getName().toLowerCase());
            if (nameComp != 0)
                return nameComp;

            return network1.getMnc().compareTo(network2.getMnc());
        }
    };
}
