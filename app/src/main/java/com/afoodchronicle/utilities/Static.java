package com.afoodchronicle.utilities;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class Static {
    //Preferences
    public static final String FACEBOOK_FIRST_NAME = "FACEBOOK_FIRST_NAME";
    public static final String FACEBOOK_LAST_NAME = "FACEBOOK_LAST_NAME";
    public static final String FACEBOOK_PROFILE_PIC = "FACEBOOK_PROFILE_PIC";
    public static final String FACEBOOK_THUMB_PROFILE_PIC = "FACEBOOK_THUMB_PROFILE_PIC";
    public static final String EMAIL_FIRST_NAME = "EMAIL_FIRST_NAME";
    public static final String EMAIL_LAST_NAME ="EMAIL_LAST_NAME";
    public static final String EMAIL_PROFILE_PIC = "EMAIL_PROFILE_PIC";
    public static final String EMAIL_THUMB_PROFILE_PIC = "EMAIL_THUMB_PROFILE_PIC";
    public static final String FACEBOOK_BIRTHDAY = "FACEBOOK_BIRTHDAY";
    public static final String FACEBOOK_DESCRIPTION = "FACEBOOK_DESCRIPTION";
    public static final String EMAIL_BIRTHDAY = "EMAIL_BIRTHDAY";
    public static final String EMAIL_DESCRIPTION = "EMAIL_DESCRIPTION";
    public static final String FACEBOOK_AGE = "FACEBOOK_AGE";
    public static final String EMAIL_AGE = "EMAIL_AGE";

    //Firebase
    public static final String JPG = ".jpg";
    public static final String IMAGES = "images/";
    public static final String THUMB_IMAGES = "thumb_images/";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String PHOTO_URL = "photoUrl";
    public static final String THUMB_PHOTO_URL = "thumbPhotoUrl";
    public static final String USERS = "users";
    public static final String PHOTO = "photo";
    public static final String DESCRIPTION = "description";
    public static final String AGE = "age";


    //Toast
    public static final String UPLOAD = "Uploading your picture...";
    public static final String UPLOAD_ERROR = "Failed to upload your picture.";

    //Error
    public static final String REQUIRED = "Required. ";
    public static final String PASSWORD_DONT_MATCH = "Passwords don't match";

    //Maps
    public static final String MARKER_NAME = "MARKER_NAME";
    public static final LatLng mYoleni = new LatLng(37.9776514, 23.7388241);
    public static final LatLng mVorria = new LatLng(37.9797024, 23.7281983);
    public static final LatLng mPnyka = new LatLng(37.9685393, 23.7478882);
    public static final LatLng mPantopoleio = new LatLng(38.0056227, 23.7826411);
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final CameraPosition ATHENS = CameraPosition.builder()
            .target(new LatLng(37.9838096, 23.7275388))
            .zoom(15)
            .build();

    // Chat
    public static final String FRAGMENT_TITLE_REQUESTS   = "Requests";
    public static final String FRAGMENT_TITLE_CHATS   = "Chats";
    public static final String FRAGMENT_TITLE_FRIENDS   = "Friends";
    public static final String VISIT_USER_ID = "visit_user_id";
    public static final String NOT_FRIENDS = "not_friends";
    public static final String REQUEST_SENT = "request_sent";
    public static final String FRIEND_REQUEST = "friendRequest";
    public static final String REQUEST_TYPE = "requestType";
    public static final String SENT = "sent";
    public static final String RECEIVED = "received";
    public static final String CANCEL_FRIEND_REQUEST = "Cancel friend request";
    public static final String SEND_FRIEND_REQUEST = "Send friend request";
    public static final String REQUEST_RECEIVED = "request_received";
    public static final String FRIENDS = "friends";
    public static final String UNFRIEND = "Unfriend this person";
    public static final String ACCEPT_FRIEND_REQUEST = "Accept friend request";

    // Images
    public static final int PICK_IMAGE_REQUEST = 71;

}
