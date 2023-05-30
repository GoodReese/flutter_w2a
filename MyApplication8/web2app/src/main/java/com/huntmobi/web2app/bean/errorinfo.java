package com.huntmobi.web2app.bean;

public class errorinfo {

    private Error error;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public static class Error {
        private String message;
        private int code;
        private String type;
        private String error_user_title;
        private String error_user_msg;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getError_user_title() {
            return error_user_title;
        }

        public void setError_user_title(String error_user_title) {
            this.error_user_title = error_user_title;
        }

        public String getError_user_msg() {
            return error_user_msg;
        }

        public void setError_user_msg(String error_user_msg) {
            this.error_user_msg = error_user_msg;
        }
    }
}
