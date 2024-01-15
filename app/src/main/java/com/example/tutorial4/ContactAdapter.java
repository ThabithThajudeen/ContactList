package com.example.tutorial4;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList;
    private Context context;

    // Constructor
    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhoneNumber());
        holder.emailTextView.setText(contact.getEmail());
        // Load the image using Glide
        String imagePath = contact.getImagePath();
        if (imagePath != null) {


            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Glide.with(context)
                        .load(imgFile)
                        .into(holder.contactImageView);
            }
        }


        // Set OnClickListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition(); // Use this position to get the correct contact item
                if (position != RecyclerView.NO_POSITION) { // Check if item still exists
                    Contact clickedContact = contactList.get(position);

                    ContactDetailFragment contactDetailFragment = new ContactDetailFragment();

                    // Pass the contact details as arguments
                    Bundle args = new Bundle();
                    args.putString("name", clickedContact.getName());
                    args.putString("phone", clickedContact.getPhoneNumber());
                    args.putString("email", clickedContact.getEmail());
                    args.putInt("position", position);
                    args.putString("imagePath", clickedContact.getImagePath());
                    contactDetailFragment.setArguments(args);

                    // Replace the current fragment with the ContactDetailFragment
                    FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, contactDetailFragment);
                    transaction.addToBackStack(null); // Add to back stack for navigation
                    transaction.commit();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, phoneTextView, emailTextView;
        ImageView contactImageView;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            emailTextView = itemView.findViewById(R.id.contact_email);
            contactImageView = itemView.findViewById(R.id.contact_image);
        }
    }
}
