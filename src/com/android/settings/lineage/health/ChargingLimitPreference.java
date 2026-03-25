/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.lineage.health;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;

import com.android.internal.lineage.health.HealthInterface;
import com.android.settingslib.widget.SliderPreference;

public class ChargingLimitPreference extends SliderPreference
        implements Preference.OnPreferenceChangeListener {

    private static final int MIN_LIMIT = 70;
    private static final int MAX_LIMIT = 100;

    private final HealthInterface mHealthInterface;

    public ChargingLimitPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mHealthInterface = HealthInterface.getInstance(context);

        setMin(MIN_LIMIT);
        setMax(MAX_LIMIT);
        setSliderIncrement(1);
        setTickVisible(true);
        setShowSliderValue(true);
        setUpdatesContinuously(true);
        setHapticFeedbackMode(HAPTIC_FEEDBACK_MODE_ON_TICKS);
        setLabelFormater(value -> ((int) value) + "%");
        setOnPreferenceChangeListener(this);
        setPersistent(false);
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        final int chargingLimit = (Integer) newValue;
        setSetting(chargingLimit);
        setSliderStateDescription(formatPercentage(chargingLimit));
        return true;
    }

    @Override
    public void setValue(final int value) {
        final int chargingLimit = clamp(value);
        setSliderStateDescription(formatPercentage(chargingLimit));
        super.setValue(chargingLimit);
    }

    protected int getSetting() {
        return clamp(mHealthInterface.getLimit());
    }

    protected void setSetting(final int chargingLimit) {
        mHealthInterface.setLimit(clamp(chargingLimit));
    }

    private int clamp(final int value) {
        return Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, value));
    }

    private String formatPercentage(final int value) {
        return value + "%";
    }
}
