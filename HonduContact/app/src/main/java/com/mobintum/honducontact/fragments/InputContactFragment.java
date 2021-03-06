package com.mobintum.honducontact.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.mobintum.honducontact.R;
import com.mobintum.honducontact.models.Contact;
import com.mobintum.honducontact.models.ContactNumber;
import com.mobintum.honducontact.models.TypeNumber;

import java.io.File;
import java.io.IOException;


public class InputContactFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM_CONTACT = "paramContact";
    private EditText etFirstname, etLastname, etCompany, etNumber, etEmail, etGithub, etFacebook, etTwitter, etInstagram;
    private ImageView imgProfile;
    private Spinner spinTypeNumber;
    private String pathPhoto;
    private String directory;
    private String fileName;
    private static final Integer TAKE_PHOTO_CODE = 1699;
    private Contact contact;

    public static InputContactFragment newInstance(Contact contact){
        InputContactFragment fragment = new InputContactFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_CONTACT, contact);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/honduContacs/";
        File newdir = new File(directory);
        if(!newdir.exists())
            newdir.mkdirs();

        if(getArguments()!=null){
            this.contact = (Contact) getArguments().getSerializable(ARG_PARAM_CONTACT);
        }
    }

    public InputContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_contact, container, false);
        etFirstname = (EditText) view.findViewById(R.id.etFirstname);
        etLastname = (EditText) view.findViewById(R.id.etLastname);
        etCompany = (EditText) view.findViewById(R.id.etCompany);
        etNumber = (EditText) view.findViewById(R.id.etNumber);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etGithub = (EditText) view.findViewById(R.id.etGithub);
        etFacebook = (EditText) view.findViewById(R.id.etFacebook);
        etTwitter = (EditText) view.findViewById(R.id.etTwitter);
        etInstagram = (EditText) view.findViewById(R.id.etInstagram);
        imgProfile = (ImageView) view.findViewById(R.id.imgProfile);
        spinTypeNumber = (Spinner) view.findViewById(R.id.spinTypeNumber);
        imgProfile.setOnClickListener(this);
        loadData();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_input_contact,menu);
        MenuItem menuSave = menu.findItem(R.id.menuSave);
        menuSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(validateFields())
                    save();
                return false;
            }
        });
    }

    private boolean validateFields(){
        boolean validate = true;
        if(etFirstname.getText().length()==0){
            validate = false;
            etFirstname.setError(getString(R.string.error_field));
        }
        if(etNumber.getText().length()==0){
            validate = false;
            etNumber.setError(getString(R.string.error_field));
        }
        return validate;
    }

    private void save(){
        String firstname = etFirstname.getText().toString();
        String lastname = etLastname.getText().toString();
        String company = etCompany.getText().toString();
        String number = etNumber.getText().toString();
        String email = etEmail.getText().toString();
        String github = etGithub.getText().toString();
        String facebook = etFacebook.getText().toString();
        String twitter = etTwitter.getText().toString();
        String instagram = etInstagram.getText().toString();
        TypeNumber typeNumber = (TypeNumber) spinTypeNumber.getSelectedItem();
        if(contact!=null){
            contact.setFirstName(firstname);
            contact.setLastName(lastname);
            contact.setCompany(company);
            contact.setEmail(email);
            contact.setGithub(github);
            contact.setFacebook(facebook);
            contact.setTwitter(twitter);
            contact.setInstagram(instagram);
            contact.setPathPhoto(pathPhoto);
            Contact.updateContact(getContext(),contact);
            ContactNumber contactNumber = ContactNumber.getContactNumbers(getContext(), contact.getContactId()).get(0);
            contactNumber.setFk_typeNumberId(typeNumber.getTypeNumberId());
            contactNumber.setNumber(number);
            ContactNumber.updateContactNumber(getContext(),contactNumber);
            Snackbar.make(getView(), getString(R.string.update_success), Snackbar.LENGTH_SHORT).show();
        }else {
            Contact contactSave = new Contact(firstname, lastname, pathPhoto, company, email, github, facebook, twitter, instagram);
            contactSave.setContactId((int) Contact.insert(getContext(), contactSave));
            long id = ContactNumber.insert(getContext(), new ContactNumber(number, typeNumber.getTypeNumberId(), contactSave.getContactId()));
            if (id != -1) {
                Snackbar.make(getView(), getString(R.string.save_success), Snackbar.LENGTH_SHORT).show();

            } else
                Snackbar.make(getView(), getString(R.string.failed_save), Snackbar.LENGTH_SHORT).show();

        }
        View v = getActivity().getCurrentFocus();
        if(v != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void loadData(){
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, TypeNumber.getTypeNumbers(getContext()));
        if(contact!=null){
            etFirstname.setText(contact.getFirstName());
            if(contact.getLastName()!=null)
                etLastname.setText(contact.getLastName());

            ContactNumber contactNumber = ContactNumber.getContactNumbers(getContext(),contact.getContactId()).get(0);
            TypeNumber typeNumber = TypeNumber.getTypeNumberById(getContext(), contactNumber.getFk_typeNumberId());

            etNumber.setText(contactNumber.getNumber());

            if(contact.getCompany()!=null)
                etCompany.setText(contact.getCompany());
            if(contact.getEmail()!=null)
                etEmail.setText(contact.getEmail());
            if(contact.getGithub()!=null)
                etGithub.setText(contact.getGithub());
            if(contact.getFacebook()!=null)
                etFacebook.setText(contact.getFacebook());
            if(contact.getTwitter()!=null)
                etTwitter.setText(contact.getTwitter());
            if(contact.getInstagram()!=null)
                etInstagram.setText(contact.getInstagram());

            spinTypeNumber.setAdapter(adapter);
            int posSpin = adapter.getPosition(typeNumber);
            spinTypeNumber.setSelection(posSpin);
            if(contact.getPathPhoto()!=null) {
                pathPhoto = contact.getPathPhoto();
                fileName = pathPhoto;
                loadPhoto();
            }

        }else {

            spinTypeNumber.setAdapter(adapter);
        }
    }


    @Override
    public void onClick(View v) {

        Long tsLong = System.currentTimeMillis()/1000;
        fileName = directory+"contact_"+tsLong.toString()+".jpg";
        File newFile = new File(fileName);
        try{
            newFile.createNewFile();
        }catch (IOException e){
            Snackbar.make(getView(),getString(R.string.failed_take_photo), Snackbar.LENGTH_SHORT).show();
            return;
        }

        Uri outputFileUri = Uri.fromFile(newFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,outputFileUri);
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }

    private void loadPhoto(){
        File imgFile = new File(fileName);
        if (imgFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgProfile.setImageBitmap(bitmap);
            pathPhoto = fileName;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == TAKE_PHOTO_CODE && resultCode == Activity.RESULT_OK){
            loadPhoto();
        }
    }
}
