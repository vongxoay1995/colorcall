package com.colorcall.callerscreen.contact;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colorcall.callerscreen.databinding.ItemContactBinding;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public ArrayList<ContactInfor> listTemp;
    public ArrayList<ContactInfor> listContact = new ArrayList<>();

    public ContactAdapter(Context context, ArrayList<ContactInfor> listContact) {
        this.context = context;
        this.listTemp = listContact;
        this.listContact.addAll(this.listTemp);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactBinding binding = ItemContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(position);
    }

    public void search(CharSequence value) {
        listContact.clear();
        if (TextUtils.isEmpty(value)) {
            listContact.addAll(listTemp);
        } else {
            for (int i = 0; i < listTemp.size(); i++) {
                String name = listTemp.get(i).getDisplayName();
                if (name.toLowerCase().contains(value)) {
                    listContact.add(listTemp.get(i));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.listContact.size();
    }

    public List<String> getContactSelected() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (ContactInfor contactInfor : this.listTemp) {
            if (contactInfor.isChecked()) {
                arrayList.add(contactInfor.getContactId());
            }
        }
        return arrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactBinding binding;
        private ContactInfor contactInfor;
        private String path;

        public ViewHolder(@NonNull ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setListeners();
        }

        public void onBind(int position) {
            contactInfor = listContact.get(position);
            if (contactInfor.getPhoto() == null) {
                path = "file:///android_asset/user.webp";
            } else {
                path = contactInfor.getPhoto();
            }
            Glide.with(context).load(path).into(binding.imgAvatar);
            binding.txtName.setText(contactInfor.getDisplayName());
            binding.imgSelectContact.setChecked(contactInfor.isChecked());
        }

        private void setListeners() {
            binding.layoutItem.setOnClickListener(v -> {
                binding.imgSelectContact.performClick();
                contactInfor.setChecked(binding.imgSelectContact.isChecked());
            });
            binding.imgSelectContact.setOnClickListener(v -> contactInfor.setChecked(binding.imgSelectContact.isChecked()));
        }
    }
}
