package com.android.internetradio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.internetradio.models.FmSavedStation;
import com.google.gson.GsonBuilder;

public class FmSharedPref {
    private final String SP_NAME_FM = "SharedPrefCarvaan";
    private Context context;
    private final String KEY_CURRENT_FM = "key_current_fm";
    private final String KEY_GA_MODEL = "key_ga_model";


    /**
     * @param context
     */
    public FmSharedPref(Context context) {
        this.context = context;
    }

    /**
     * @param fmStation
     */
    public void saveCurrentStation(FmSavedStation fmStation) {
        String string = new GsonBuilder().create().toJson(fmStation);
        SharedPreferences sp = context.getSharedPreferences(SP_NAME_FM,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_CURRENT_FM, string);
        editor.commit();
    }

    /**
     * @return
     */
    public FmSavedStation getCurrentStation() {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME_FM,
                Context.MODE_PRIVATE);
        String string = sp.getString(KEY_CURRENT_FM, null);

        if (TextUtils.isEmpty(string)) {
            return null;
        } else {
            /*Type typeOfSrc = new TypeToken<T>() {
            }.getType();*/
            return new GsonBuilder().create().fromJson(string, FmSavedStation.class);
        }
    }


//    /**
//     * @param gaModel
//     */
//    public void saveGaModel(GAModel gaModel) {
//        String string = new GsonBuilder().create().toJson(gaModel);
//        SharedPreferences sp = context.getSharedPreferences(SP_NAME_FM,
//                Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString(KEY_GA_MODEL, string);
//        editor.apply();
//    }
//
//
//    /**
//     * @return
//     */
//    public GAModel getGAModel() {
//        SharedPreferences sp = context.getSharedPreferences(SP_NAME_FM,
//                Context.MODE_PRIVATE);
//        String string = sp.getString(KEY_GA_MODEL, null);
//
//        if (TextUtils.isEmpty(string)) {
//            return null;
//        } else {
//            /*Type typeOfSrc = new TypeToken<T>() {
//            }.getType();*/
//            return new GsonBuilder().create().fromJson(string, GAModel.class);
//        }
//    }
}
