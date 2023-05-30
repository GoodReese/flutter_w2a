package com.huntmobi.web2app.bean;

import java.util.List;

public class purchaseinfo {

    private DeviceId device_id;
    private CustomData custom_data;
    private String w2a_data_encrypt;

    public DeviceId getDevice_id() {
        return device_id;
    }

    public void setDevice_id(DeviceId device_id) {
        this.device_id = device_id;
    }

    public CustomData getCustom_data() {
        return custom_data;
    }

    public void setCustom_data(CustomData custom_data) {
        this.custom_data = custom_data;
    }

    public String getW2a_data_encrypt() {
        return w2a_data_encrypt;
    }

    public void setW2a_data_encrypt(String w2a_data_encrypt) {
        this.w2a_data_encrypt = w2a_data_encrypt;
    }

    public static class DeviceId {
        private String imei;
        private String android_ID;
        private String advertiser_ID;
        private String idfv;
        private String idfa;

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getAndroid_ID() {
            return android_ID;
        }

        public void setAndroid_ID(String android_ID) {
            this.android_ID = android_ID;
        }

        public String getAdvertiser_ID() {
            return advertiser_ID;
        }

        public void setAdvertiser_ID(String advertiser_ID) {
            this.advertiser_ID = advertiser_ID;
        }

        public String getIdfv() {
            return idfv;
        }

        public void setIdfv(String idfv) {
            this.idfv = idfv;
        }

        public String getIdfa() {
            return idfa;
        }

        public void setIdfa(String idfa) {
            this.idfa = idfa;
        }
    }

    public static class CustomData {
        private String event_id;
        private String event_name;
        private String currency;
        private String value;
        private String content_type;
        private List<String> content_ids;

        public String getEvent_id() {
            return event_id;
        }

        public void setEvent_id(String event_id) {
            this.event_id = event_id;
        }

        public String getEvent_name() {
            return event_name;
        }

        public void setEvent_name(String event_name) {
            this.event_name = event_name;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getContent_type() {
            return content_type;
        }

        public void setContent_type(String content_type) {
            this.content_type = content_type;
        }

        public List<String> getContent_ids() {
            return content_ids;
        }

        public void setContent_ids(List<String> content_ids) {
            this.content_ids = content_ids;
        }
    }
}
