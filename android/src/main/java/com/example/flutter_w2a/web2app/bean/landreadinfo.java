package com.huntmobi.web2app.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class landreadinfo {
    public String getCA() {
        return CA;
    }

    public void setCA(String CA) {
        this.CA = CA;
    }

    public String getWG() {
        return WG;
    }

    public void setWG(String WG) {
        this.WG = WG;
    }

    public String getPI() {
        return PI;
    }

    public void setPI(String PI) {
        this.PI = PI;
    }

    public String getAO() {
        return AO;
    }

    public void setAO(String AO) {
        this.AO = AO;
    }

    public String getSE() {
        return SE;
    }

    public void setSE(String SE) {
        this.SE = SE;
    }

    public String getFT() {
        return FT;
    }

    public void setFT(String FT) {
        this.FT = FT;
    }
    @JSONField(name = "CA")
    private String CA = "";
    @JSONField(name = "WG")
    private String WG = "";
    @JSONField(name = "PI")
    private String PI = "";
    @JSONField(name = "AO")
    private String AO = "";
    @JSONField(name = "SE")
    private String SE = "";
    @JSONField(name = "FT")
    private String FT = "";
}
