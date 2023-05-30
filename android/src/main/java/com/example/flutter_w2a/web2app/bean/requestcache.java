package com.huntmobi.web2app.bean;

import java.util.ArrayList;

public class requestcache {
    public ArrayList<cacheitem> getCacheitems() {
        return cacheitems;
    }

    public void setCacheitems(ArrayList<cacheitem> cacheitems) {
        this.cacheitems = cacheitems;
    }

    private ArrayList<cacheitem> cacheitems;
    public static class cacheitem {
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        private  String url;
        private  String content;
    }
}
