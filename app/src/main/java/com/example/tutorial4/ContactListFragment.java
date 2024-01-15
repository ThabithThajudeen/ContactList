package com.example.tutorial4;

import android.Manifest;


import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ContactListFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton addContactButton;


    private ContactViewModel contactViewModel;

    private static final int YOUR_REQUEST_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set the adapter for recyclerView here
        // Assuming you have a method to fetch contacts from the database

        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        contactViewModel.getContacts().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                // Update your RecyclerView adapter data here
                ContactAdapter contactAdapter = new ContactAdapter(getContext(), contacts);
                recyclerView.setAdapter(contactAdapter);
            }
        });

        addContactButton = view.findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddContactFragment addContactFragment = new AddContactFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addContactFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        Button importContactsButton = view.findViewById(R.id.import_contacts_button);
        importContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importDeviceContacts();
            }
        });



        return view;
    }


    private void importDeviceContacts() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, YOUR_REQUEST_CODE);
            return;
        }

        // Logic to fetch and add contacts goes here
        readAndAddContacts();
    }

    private void readAndAddContacts() {
        List<Contact> deviceContacts = new ArrayList<>();

        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Email.DATA
        };

        Cursor cursor = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int emailColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                if (nameColumnIndex != -1 && phoneColumnIndex != -1 && emailColumnIndex != -1) {
                    String name = cursor.getString(nameColumnIndex);
                    String phoneNumber = cursor.getString(phoneColumnIndex);
                    String email = cursor.getString(emailColumnIndex);
                    Contact newContact = new Contact(0,name, email, phoneNumber, null);
                    deviceContacts.add(newContact);
                }

            }
            cursor.close();
        }

        List<Contact> existingContacts = contactViewModel.getContacts().getValue();



        for (Contact deviceContact : deviceContacts) {
            boolean isDuplicate = false;
            for (Contact existingContact : existingContacts) {
                // A simple check based on name, phone, and email. You can expand on this.
                if (deviceContact.getName().equals(existingContact.getName()) &&
                        deviceContact.getPhoneNumber().equals(existingContact.getPhoneNumber()) ||
                        deviceContact.getEmail().equals(existingContact.getEmail())) { // Check all attributes before concluding that it's a duplicate.
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                contactViewModel.addContact(deviceContact);
            }
        }



        // Notify the user
        Toast.makeText(getContext(), "Contacts imported successfully!", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == YOUR_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. Now import the contacts.
                readAndAddContacts();
            } else {
                Toast.makeText(getContext(), "Permission denied! Can't import contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
