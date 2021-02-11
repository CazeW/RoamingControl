package net.caze.roamingcontrol;


import java.io.Serializable;
import java.util.Comparator;

class Country implements Serializable {
    private String name, mcc, mcc_text;

    public Country() {}

    public Country(String name, String mcc) {
        this.name = name;
        this.mcc = mcc;
        this.mcc_text = "MCC: " + mcc + " ";
    }

    String getName() {
        return name;
    }

    String getMcc() {
        return mcc;
    }
    String getMcc_text() {
        return mcc_text;
    }


    static Comparator<Country> nameComparator = new Comparator<Country>() {
        @Override
        public int compare(Country country1, Country country2) {
            return country1.getName().compareTo(country2.getName());
        }
    };
}
