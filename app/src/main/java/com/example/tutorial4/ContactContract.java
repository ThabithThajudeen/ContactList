package com.example.tutorial4;

import android.provider.BaseColumns;

public final class ContactContract {

    private ContactContract() {}

    public static class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PHOTO = "photo";
        public static final String COLUMN_IMAGE_PATH = "image_path";

    }
}
