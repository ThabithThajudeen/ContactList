package com.example.tutorial4;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContactDetailFragment extends Fragment {

    private EditText nameEditText, phoneEditText, emailEditText;
    private ImageView contactImageView;
    private Button saveChangesButton;

    private Button deleteButton;
    private ContactViewModel contactViewModel;
    private int contactPosition;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentImagePath = null;
    private Button captureImageButton;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                if (permissions.get(Manifest.permission.CAMERA) && permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    launchCamera();
                } else {
                    Toast.makeText(getContext(), "Camera and storage permissions are required!", Toast.LENGTH_SHORT).show();
                }
            }
    );










    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_detail, container, false);

        // Initialize UI Elements
        nameEditText = view.findViewById(R.id.contact_name);
        phoneEditText = view.findViewById(R.id.contact_phone);
        emailEditText = view.findViewById(R.id.contact_email);
        contactImageView = view.findViewById(R.id.contact_image);
        saveChangesButton = view.findViewById(R.id.button_save_changes);
        deleteButton = view.findViewById(R.id.delete_button);

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Retrieve contact details from arguments
        Bundle args = getArguments();
        if (args != null) {
            nameEditText.setText(args.getString("name"));
            phoneEditText.setText(args.getString("phone"));
            emailEditText.setText(args.getString("email"));
            contactPosition = args.getInt("position", -1);

            // Load the image using Glide
            String imagePath = args.getString("imagePath");
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(this)
                        .load(imagePath)
                        .into(contactImageView);
            }
            // Load the image or any other data as needed...
        }

        // Handle Save Changes Button Click
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedName = nameEditText.getText().toString();
                String updatedPhone = phoneEditText.getText().toString();
                String updatedEmail = emailEditText.getText().toString();

                if (contactPosition != -1) {
                    // Pass the currentImagePath to the updateContact method
                    contactViewModel.updateContact(contactPosition, updatedName, updatedPhone, updatedEmail, currentImagePath);
                }

                ContactListFragment contactListFragment = new ContactListFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, contactListFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });



        // Set Delete Button onClickListener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactViewModel.deleteContact(contactPosition); // Remove the contact from ViewModel list

                // Switch back to ContactListFragment
                ContactListFragment contactListFragment = new ContactListFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, contactListFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        captureImageButton = view.findViewById(R.id.button_capture_image);

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cameraPermissionGranted = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                boolean storagePermissionGranted = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                if (cameraPermissionGranted || storagePermissionGranted) {
                    launchCamera();
                } else {
                    requestPermissionLauncher.launch(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                // Inform the user about the importance of permissions
            }
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Toast.makeText(getContext(), "in Launch Camera!", Toast.LENGTH_SHORT).show();
            Uri photoURI = null;
            try {
                photoURI = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }
            if (photoURI != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        //} else {
         //   Toast.makeText(getContext(), "No camera app found!", Toast.LENGTH_SHORT).show();
        //}
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
            contactImageView.setImageBitmap(bitmap);
        }
    }



    private Uri createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentImagePath = image.getAbsolutePath();
        return FileProvider.getUriForFile(getActivity(),
                "com.example.tutorial4.fileprovider",
                image);
    }
}
