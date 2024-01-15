package com.example.tutorial4;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>();
    private final ContactDbHelper dbHelper;

    public ContactViewModel(Application application) {
        super(application);
        dbHelper = new ContactDbHelper(application);
        loadContacts();
    }

    private void loadContacts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ContactContract.ContactEntry._ID,
                ContactContract.ContactEntry.COLUMN_NAME,
                ContactContract.ContactEntry.COLUMN_PHONE,
                ContactContract.ContactEntry.COLUMN_EMAIL,
                ContactContract.ContactEntry.COLUMN_IMAGE_PATH,
                // Add other columns as needed
        };

        Cursor cursor = db.query(
                ContactContract.ContactEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<Contact> contactList = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NAME));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_EMAIL));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_IMAGE_PATH));

            // Retrieve other columns as needed
            contactList.add(new Contact(id, name, email, phone, imagePath));
        }
        cursor.close();
        contacts.setValue(contactList);
    }

    public void addContact(Contact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ContactContract.ContactEntry.COLUMN_NAME, contact.getName());
        values.put(ContactContract.ContactEntry.COLUMN_PHONE, contact.getPhoneNumber());
        values.put(ContactContract.ContactEntry.COLUMN_EMAIL, contact.getEmail());
        values.put(ContactContract.ContactEntry.COLUMN_IMAGE_PATH, contact.getImagePath());

        // Add other columns as needed

        long newRowId = db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values);

        if (newRowId != -1) {
            loadContacts(); // Refresh the contact list
        }
    }

    public void updateContact(int position, String updatedName, String updatedPhone, String updatedEmail, String updatedImagePath) {
        if (contacts.getValue() != null && position >= 0 && position < contacts.getValue().size()) {
            Contact contactToUpdate = contacts.getValue().get(position);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(ContactContract.ContactEntry.COLUMN_NAME, updatedName);
            values.put(ContactContract.ContactEntry.COLUMN_PHONE, updatedPhone);
            values.put(ContactContract.ContactEntry.COLUMN_EMAIL, updatedEmail);
            values.put(ContactContract.ContactEntry.COLUMN_IMAGE_PATH, updatedImagePath);  // Assuming this column exists in your database

            String selection = ContactContract.ContactEntry._ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(contactToUpdate.getId()) };

            int count = db.update(
                    ContactContract.ContactEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs
            );

            if (count > 0) {
                loadContacts(); // Refresh the contact list
            }
        }
    }


    public void deleteContact(int position) {
        if (contacts.getValue() != null && position >= 0 && position < contacts.getValue().size()) {
            Contact contactToDelete = contacts.getValue().get(position);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String selection = ContactContract.ContactEntry._ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(contactToDelete.getId()) };

            int deletedRows = db.delete(ContactContract.ContactEntry.TABLE_NAME, selection, selectionArgs);

            if (deletedRows > 0) {
                loadContacts(); // Refresh the contact list
            }
        }
    }

    // Inside ContactViewModel

    // This will be used to save image to device storage and return the path
    public String saveImageToInternalStorage(Bitmap bitmapImage){
        Context context = getApplication();
        String fileName = "contact_" + System.currentTimeMillis() + ".jpg";
        File directory = context.getDir("contact_images", Context.MODE_PRIVATE);
        File myPath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(directory, fileName).getAbsolutePath();

    }


    public MutableLiveData<List<Contact>> getContacts() {
        return contacts;
    }
}
