package com.tools.calculator.util.fractionutil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tools.calculator.R;

public class CalculatorSetting {

    public static final String INPUT_MATH = "inp_math";

    public static final String INPUT_BASE = "INPUT_BASE";
    public static final String RESULT_BASE = "RESULT_BASE";
    public static final String BASE = "BASE";

    public static final String IS_FRACTION = "fraction_mode";

    public static final String NOTIFY_UPDATE = "NOTIFY_UPDATE";
    private static final String IS_UPDATE = "IS_UPDATE_";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private Context context;

    @SuppressLint("CommitPrefEdits")
    public CalculatorSetting(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public CalculatorSetting(SharedPreferences mPreferences, Context context) {
        this.context = context;
        this.sharedPreferences = mPreferences;
        this.editor = sharedPreferences.edit();
    }

    public static EvaluateConfig createEvaluateConfig(Context context) {
        CalculatorSetting setting = new CalculatorSetting(context);
        EvaluateConfig config = EvaluateConfig.newInstance();
        config.setEvalMode(setting.useFraction() ? EvaluateConfig.FRACTION : EvaluateConfig.DECIMAL);
        config.setRoundTo(setting.getPrecision());
        return config;
    }

    public boolean useLightTheme() {
        return sharedPreferences.getBoolean("THEME_STYLE", false);
    }

    public boolean useFraction() {
        return sharedPreferences.getBoolean(CalculatorSetting.IS_FRACTION, false);
    }

    public void setFraction(boolean b) {
        editor.putBoolean(IS_FRACTION, b).commit();
    }

    public boolean isUpdate(Context context) {
        return !(new CalculatorSetting(context).getBoolean(IS_UPDATE));
    }

    public void setUpdate(boolean b) {
        put(IS_UPDATE, b);
    }

    public void put(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void put(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int def) {
        try {
            return sharedPreferences.getInt(key, def);
        } catch (Exception e) {
            String value = getString(context.getString(R.string.key_pref_precision));
            try {
                return Integer.parseInt(value);
            } catch (Exception e1) {

            }
        }
        return def;
    }

    /**
     * get long value from key,
     *
     * @param key - key
     * @return -1 if not found
     */
    public long getLong(String key) {
        try {
            return sharedPreferences.getLong(key, -1);
        } catch (Exception e) {
            return -1;
        }
    }

    public String getString(String key) {
        try {
            return sharedPreferences.getString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    public String getString(String key, String def) {
        try {
            return sharedPreferences.getString(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public void setVar(String name, String value) {
        editor.putString("var_" + name, value);
    }

    public void setNotifyUpdate(boolean b) {
        editor.putBoolean(NOTIFY_UPDATE, b).apply();
    }

    public boolean useFullScreen() {
        return getBoolean(context.getString(R.string.key_hide_status_bar));
    }

    public SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }

    public int getPrecision() {
        return getInt(context.getString(R.string.key_pref_precision));
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener
                                                                 onSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener
                                                                   onSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);

    }
}