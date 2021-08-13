package com.colorcall.callerscreen.analystic;

import android.os.Bundle;

public class ManagerEvent {
    //splash tracking
    public static Event splashOpen(){
        return new Event(EventKey.SPLASH_OPEN,new Bundle());
    }
    public static Event splashStart(){
        return new Event(EventKey.SPLASH_START_CLICKED,new Bundle());
    }
    //main tracking
    public static Event mainOpen(){
        return new Event(EventKey.MAIN_OPEN,new Bundle());
    }
    public static Event mainAdsOpen(){
        return new Event(EventKey.MAIN_ADSBOX_CLICKED,new Bundle());
    }
    public static Event mainCameraOpen(){
        return new Event(EventKey.MAIN_CAMERA_CLICKED,new Bundle());
    }
    public static Event mainSeeAllColorEffect(){
        return new Event(EventKey.MAIN_SEEALLCOLOREFFECT_CLICKED,new Bundle());
    }
    public static Event mainSeeAllLovelly(){
        return new Event(EventKey.MAIN_SEEALLLOVELY_CLICKED,new Bundle());
    }
    public static Event mainSeeAllRecommend(){
        return new Event(EventKey.MAIN_SEEALLRECOMMEND_CLICKED,new Bundle());
    }
    public static Event mainSeeAllPopulated(){
        return new Event(EventKey.MAIN_SEEALLPOPULAR_CLICKED,new Bundle());
    }
    public static Event mainSeeAllYourTheme(){
        return new Event(EventKey.MAIN_SEEALLYOURTHEME_CLICKED,new Bundle());
    }
    public static Event mainSlideClick(){
        return new Event(EventKey.MAIN_SLIDEMENU_CLICKED,new Bundle());
    }
    public static Event mainStarClick(){
        return new Event(EventKey.MAIN_STAR_CLICKED,new Bundle());
    }
    public static Event mainDialogOpen(){
        return new Event(EventKey.MAINDIALOG_OPEN,new Bundle());
    }
    public static Event mainDialogVideo(){
        return new Event(EventKey.MAINDIALOG_VIDEOS_CLICKED,new Bundle());
    }
    public static Event mainDialogPicture(){
        return new Event(EventKey.MAINDIALOG_IMAGES_CLICKED,new Bundle());
    }
    //seemore tracking
    public static Event seeMoreOpen(){
        return new Event(EventKey.SEEMORE_OPEN,new Bundle());
    }
    public static Event seeMoreBack(){
        return new Event(EventKey.SEEMORE_BACK_CLICKED,new Bundle());
    }
    public static Event seeMoreCamera(){
        return new Event(EventKey.SEEMORE_CAMERA_CLICKED,new Bundle());
    }
    //apply tracking
    public static Event applyOpen(){
        return new Event(EventKey.APPLY_OPEN,new Bundle());
    }
    public static Event applyAdsClick(){
        return new Event(EventKey.APPLY_ADSBOX_CLICKED,new Bundle());
    }
    public static Event applyItemVideo(int index,String name){
        return new Event(EventKey.APPLY_ITEM+"_"+index+"_"+name,new Bundle());
    }
    public static Event applyItemImage(int index,String name){
        return new Event(EventKey.APPLY_ITEM+"_"+index+"_"+name,new Bundle());
    }
    public static Event applyVideoViewError(int what,int extra){
        Bundle bundle =  new Bundle();
        bundle.putInt("what",what);
        bundle.putInt("extra",extra);
        return new Event("ApplyVideoViewError",bundle);
    }
    public static Event applyVideoThemeSelected(String name) {
        Bundle bundle = new Bundle();
        bundle.putString("Video_Theme_selected",name);
        return new Event("Video_Theme_Apply",bundle);
    }
    public static Event applyImageThemeSelected(String name) {
        Bundle bundle = new Bundle();
        bundle.putString("Image_Theme_selected",name);
        return new Event("Image_Theme_Apply",bundle);
    }
    public static Event applyApplyClick(){
        return new Event(EventKey.APPLY_APPLY_CLICKED,new Bundle());
    }
    public static Event applyBackClick(){
        return new Event(EventKey.APPLY_BACK_CLICKED,new Bundle());
    }
    public static Event applyBinClick(){
        return new Event(EventKey.APPLY_BIN_CLICKED,new Bundle());
    }
    public static Event applyContactClick(){
        return new Event(EventKey.APPLY_CONTACT_CLICKED,new Bundle());
    }
    public static Event applyStarClick(){
        return new Event(EventKey.APPLY_STAR_CLICKED,new Bundle());
    }
    public static Event applyDialogNopeClick(){
        return new Event(EventKey.APPLYDIALOG_NOPE_CLICKED,new Bundle());
    }
    public static Event applyDialogOpen(){
        return new Event(EventKey.APPLYDIALOG_OPEN,new Bundle());
    }
    public static Event applyDialogSureClick(){
        return new Event(EventKey.APPLYDIALOG_SURE_CLICKED,new Bundle());
    }
    //setting
    public static Event settingOpen(){
        return new Event(EventKey.SETTING_OPEN,new Bundle());
    }
    public static Event settingAdsClick(){
        return new Event(EventKey.SETTING_ADSBOX_CLICKED,new Bundle());
    }
    public static Event settingBackClick(){
        return new Event(EventKey.SETTING_BACK_CLICKED,new Bundle());
    }
    public static Event settingCheckUpdateClick(){
        return new Event(EventKey.SETTING_CHECKUPDATE_CLICKED,new Bundle());
    }
    public static Event settingGetVipClick(){
        return new Event(EventKey.SETTING_GETVIP_CLICKED,new Bundle());
    }
    public static Event settingPolicyClick(){
        return new Event(EventKey.SETTING_POLICY_CLICKED,new Bundle());
    }
    public static Event settingShareAppClick(){
        return new Event(EventKey.SETTING_SHAREAPP_CLICKED,new Bundle());
    }
    //rate
    public static Event rateShow(){
        return new Event(EventKey.RATE_SHOW,new Bundle());
    }
    public static Event rateFeedBack(){
        return new Event(EventKey.RATE_FEED_BACK,new Bundle());
    }
    public static Event rateNotNow(){
        return new Event(EventKey.RATE_NOT_NOW,new Bundle());
    }
    public static Event rateReview(int value){
        Bundle bundle = new Bundle();
        bundle.putInt("rate",value);
        return new Event(EventKey.RATE_REVIEW,bundle);
    }

    //contact
    public static Event contactOpen(){
        return new Event(EventKey.SELECT_CONTACT_OPEN,new Bundle());
    }
    public static Event contactSet(){
        return new Event(EventKey.SELECT_CONTACT_SET,new Bundle());
    }
    public static Event contactSearch(){
        return new Event(EventKey.SELECT_CONTACT_SEARCH,new Bundle());
    }

    //call screen show
    public static Event callshow(){
        return new Event(EventKey.CALL_SHOW,new Bundle());
    }
    public static Event callAcceptCall(){
        return new Event(EventKey.CALL_ACCETP_CALL,new Bundle());
    }
    public static Event callRejectCall(){
        return new Event(EventKey.CALL_REJECT_CALL,new Bundle());
    }
    public static Event callDismiss(){
        return new Event(EventKey.CALL_DISMISS,new Bundle());
    }
    public static Event callSmallAndroidP(){
        return new Event(EventKey.CALL_ANDROID_SMALL_P,new Bundle());
    }
    public static Event callBiggerAndroidP(){
        return new Event(EventKey.CALL_ANDROID_BIGGER_P,new Bundle());
    }
    public static Event callVideoViewError(int what,int extra){
        Bundle bundle =  new Bundle();
        bundle.putInt("what",what);
        bundle.putInt("extra",extra);
        return new Event("CallVideoViewError",bundle);
    }
    // position main tracking
    public static Event mainRecoClick(int position){
        return new Event("MAIN_RECOPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event mainPopuClick(int position){
        return new Event("MAIN_POPUPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event mainLovelyClick(int position){
        return new Event("MAIN_LOVEPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event mainColorEffectClick(int position){
        return new Event("MAIN_COLORPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event mainYourPictureClick(int position){
        return new Event("MAIN_YOURPICTURE_"+position+"_CLICKED",new Bundle());
    }
    // position seemore tracking
    public static Event seemoreRecoClick(int position){
        return new Event("SEEMORE_RECOPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event seemorePopuClick(int position){
        return new Event("SEEMORE_POPUPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event seemoreLovelyClick(int position){
        return new Event("SEEMORE_LOVEPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event seemoreColorEffectClick(int position){
        return new Event("SEEMORE_COLORPICTURE_"+position+"_CLICKED",new Bundle());
    }
    public static Event seemoreYourPictureClick(int position){
        return new Event("SEEMORE_YOURPICTURE_"+position+"_CLICKED",new Bundle());
    }
}
