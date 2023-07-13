package com.colorcall.callerscreen.contact;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.custom.CircleSelectImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false));
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
        ArrayList arrayList = new ArrayList();
        for (ContactInfor contactInfor : this.listTemp) {
            if (contactInfor.isChecked()) {
                arrayList.add(contactInfor.getContactId());
            }
        }
        return arrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgAvatar)
        CircleImageView imgAvatar;
        @BindView(R.id.imgSelectContact)
        CircleSelectImageView imgSelectContact;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.layoutItem)
        LinearLayout layoutItem;
        private ContactInfor contactInfor;
        private String path;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int position) {
            contactInfor = listContact.get(position);
            if (contactInfor.getPhoto() == null) {
                path = "file:///android_asset/user.webp";
            } else {
                path = contactInfor.getPhoto();
            }
            Glide.with(context).load(path).into(imgAvatar);
            txtName.setText(contactInfor.getDisplayName());
            imgSelectContact.setChecked(contactInfor.isChecked());
        }

        @OnClick({R.id.layoutItem, R.id.imgSelectContact})
        public void onViewClicked(View view) {
            switch (view.getId()) {
                case R.id.layoutItem:
                    imgSelectContact.performClick();
                    contactInfor.setChecked(imgSelectContact.isChecked());
                    return;
                case R.id.imgSelectContact:
                    contactInfor.setChecked(imgSelectContact.isChecked());
                    return;
                default:
            }
        }
    }
}
