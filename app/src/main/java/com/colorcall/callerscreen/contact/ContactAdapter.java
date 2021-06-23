package com.colorcall.callerscreen.contact;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorcall.callerscreen.R;
import com.colorcall.callerscreen.custom.CircleSelectImageView;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.database.Contact;
import com.colorcall.callerscreen.utils.HawkHelper;
import com.colorcall.callerscreen.video.VideoAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public ArrayList<ContactInfor> listContact;

    public ContactAdapter(Context context, ArrayList<ContactInfor> listContact) {
        this.context = context;
        this.listContact = listContact;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return this.listContact.size();
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
            Log.e("TAN", "onBind: "+contactInfor.getPhoto());
            if(contactInfor.getPhoto()==null){
                path = "file:///android_asset/user.webp";
            }else {
                path = contactInfor.getPhoto();
            }
            Glide.with(context).load(path).into(imgAvatar);
            txtName.setText(contactInfor.getDisplayName());
            imgSelectContact.setChecked(contactInfor.isChecked());
        }
    }
}
