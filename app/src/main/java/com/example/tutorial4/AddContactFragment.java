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
import android.util.Log;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddContactFragment extends Fragment {

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private ImageView contactImageView;
    private Button captureImageButton;
    private Button saveContactButton;
    private ContactViewModel contactViewModel;


    private static final int REQUEST_IMAGE_CAPTURE = 1;
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
    private String currentImagePath = null;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        // Initialize UI Elements
        nameEditText = view.findViewById(R.id.edit_text_name);
        phoneEditText = view.findViewById(R.id.edit_text_phone);
        emailEditText = view.findViewById(R.id.edit_text_email);
        contactImageView = view.findViewById(R.id.contact_image);
        captureImageButton = view.findViewById(R.id.button_capture_image);
        saveContactButton = view.findViewById(R.id.button_save_contact);
        // Inside onCreateView() method

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cameraPermissionGranted = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                boolean storagePermissionGranted = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                if (cameraPermissionGranted || storagePermissionGranted) {
                    launchCamera();
                } else {
                    requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            }
        });

        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        // Handle Save Contact Button Click
        saveContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get User Input
                String name = nameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();

                // Check if fields are empty
                if (name.isEmpty() || phone.isEmpty() ) {
                    Toast.makeText(getContext(), "Please fill in essential fields!", Toast.LENGTH_SHORT).show();
                    return; // exit the method early
                }

                // Implement Saving Contact Here (e.g. to Database)
                if (currentImagePath != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
                    String savedImagePath = contactViewModel.saveImageToInternalStorage(bitmap);
                    contactViewModel.addContact(new Contact(0, name, email, phone, savedImagePath));
                } else {
                    contactViewModel.addContact(new Contact(0, name, email, phone, null));
                }


                // Once saved, go back to Contact List Fragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ContactListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
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
      //  if (cameraIntent.resolveActivity(getActivity().getPackageManager())!= null) {
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
     //   } else {
       //     Toast.makeText(getContext(), "No camera app found!", Toast.LENGTH_SHORT).show();
       // }
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
