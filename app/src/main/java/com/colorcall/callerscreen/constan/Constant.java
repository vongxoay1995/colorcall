package com.colorcall.callerscreen.constan;

import android.os.Environment;

public interface Constant {
    String DATA_TYPE = "text/plain";
    String THUMB_DEFAULT = "thumbDefault";
    String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=";
    String POLICY_URL = "https://bluewavevn.wordpress.com/2019/12/20/phone-caller-screen-color-call-app-privacy-policy/";
    int REQUEST_VIDEO = 1;
    int REQUEST_OVERLAY = 0 ;
    int REQUEST_NOTIFICATION_ACCESS =3 ;
    String SHOW_IMG_DELETE = "delete";
    String BACKGROUND = "background";
    int REQUEST_CODE_IMAGES = 2;
    String PATH_THUMB_COLOR_CALL = "/ColorCall/Video/thums/" ;
    String PATH_THUMB_COLOR_CALL_IMAGES = "/ColorCall/Images";
    String PROVIDER = ".provider";
    int PERMISSION_REQUEST_CODE_CAMERA = 99;
    int PERMISSION_REQUEST_CODE_CALL_PHONE = 98;
    String PHONE_NUMBER = "phone_number";
    int TYPE_VIDEO = 0;
    int TYPE_IMAGE = 1;
    String ID_INTER_TEST = "ca-app-pub-3940256099942544/1033173712";
    String ID_NATIVE_TEST = "ca-app-pub-3940256099942544/2247696110";
    String ID_TEST_BANNER_ADMOD = "ca-app-pub-3940256099942544/6300978111";
    String IS_UPDATE_LIST = "updateList";
    String INTENT_APPLY_THEME = "applyTheme";
    String ACTION_LOAD_COMPLETE_THEME = "loadCompleteTheme";
    String BASE_URL = "http://smartappvn.com";
    String LINK_VIDEO_CACHE = Environment.getDataDirectory().toString()+"/data/com.colorcall.callerscreen/background/" ;
    String INTENT_DELETE_THEME = "deleteTheme";
    String INTENT_DOWNLOAD_COMPLETE_THEME = "downloadTheme" ;
    String REFRESH_All = "refresh_all";
    String ITEM_POSITION = "item_position";
    String FROM_SCREEN = "from_screen";
    int IMAGES_FRAG_MENT = 1;
    int VIDEO_FRAG_MENT = 2;
    int MYTHEME_FRAG_MENT = 3;
    String APPLY_ITEM_DEFAULT = "apply_default";
    String CAPTURE_IMAGE_PATH = "capture_image_path";
    String THUMB_DIR = "thumb_";
    int PERMISSIONS_REQUEST_READ_CONTACTS = 97;
    String [] nameRandom = {"Albert Flores","Wade Warren","Kristin Watson","Robert Fox","Theresa Webb","Jane Cooper","Annette Black","Dianne Russell","Kathryn Murphy","Ronald Richards"};
    String [] phoneRandom = {"(302) 555-0107","(671) 555-0110","(907) 555-0101","(239) 555-0108","(308) 555-0121","(208) 555-0112","(225) 555-0118","(704) 555-0127","(406) 555-0120","(603) 555-0123"};
    String [] avatarRandom = {"avar1.webp","avar2.webp","avar3.webp","avar4.webp","avar5.webp","avar6.webp","avar7.webp","avar8.webp","avar9.webp","avar91.webp"};
    String TYPE_PROMPT = "TYPE_PROMPT";
    String POS_RANDOM = "pos_random";
}
