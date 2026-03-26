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

import static android.view.HapticFeedbackConstants.CLOCK_TICK;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.internal.lineage.health.HealthInterface;
import com.android.settings.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class ChargingLimitPreference extends Preference
        implements Slider.OnChangeListener, Slider.OnSliderTouchListener {

    private static final int MIN_LIMIT = 70;
    private static final int MAX_LIMIT = 95;
    private static final int STEP_SIZE = 5;

    private final HealthInterface mHealthInterface;

    private Slider mChargingLimitSlider;
    private int mLastHapticValue = Integer.MIN_VALUE;

    public ChargingLimitPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference_charging_limit);
        setSelectable(false);

        mHealthInterface = HealthInterface.getInstance(context);
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mChargingLimitSlider = (Slider) holder.findViewById(R.id.slider);
        if (mChargingLimitSlider == null) {
            return;
        }

        final int currentLimit = getSetting();

        mChargingLimitSlider.clearOnChangeListeners();
        mChargingLimitSlider.clearOnSliderTouchListeners();
        mChargingLimitSlider.setValueFrom(MIN_LIMIT);
        mChargingLimitSlider.setValueTo(MAX_LIMIT);
        mChargingLimitSlider.setStepSize(STEP_SIZE);
        mChargingLimitSlider.setLabelBehavior(LabelFormatter.LABEL_FLOATING);
        mChargingLimitSlider.setLabelFormatter(value -> formatPercentage(Math.round(value)));
        mChargingLimitSlider.setEnabled(isEnabled());
        mChargingLimitSlider.setValue(currentLimit);
        mChargingLimitSlider.setStateDescription(formatPercentage(currentLimit));
        mChargingLimitSlider.addOnChangeListener(this);
        mChargingLimitSlider.addOnSliderTouchListener(this);
    }

    @Override
    public void onValueChange(final Slider slider, final float value, final boolean fromUser) {
        final int chargingLimit = sanitizeValue(Math.round(value));
        slider.setStateDescription(formatPercentage(chargingLimit));

        if (!fromUser) {
            return;
        }

        if (chargingLimit != mLastHapticValue) {
            slider.performHapticFeedback(CLOCK_TICK);
            mLastHapticValue = chargingLimit;
        }

        setSetting(chargingLimit);
    }

    @Override
    public void onStartTrackingTouch(final Slider slider) {
        mLastHapticValue = sanitizeValue(Math.round(slider.getValue()));
    }

    @Override
    public void onStopTrackingTouch(final Slider slider) {
        final int chargingLimit = sanitizeValue(Math.round(slider.getValue()));
        slider.setStateDescription(formatPercentage(chargingLimit));
        setSetting(chargingLimit);
    }

    public void setValue(final int value) {
        final int chargingLimit = sanitizeValue(value);
        if (mChargingLimitSlider != null) {
            mChargingLimitSlider.setValue(chargingLimit);
            mChargingLimitSlider.setStateDescription(formatPercentage(chargingLimit));
        }
    }

    protected int getSetting() {
        final int currentValue = mHealthInterface.getLimit();
        final int sanitizedValue = sanitizeValue(currentValue);
        if (sanitizedValue != currentValue) {
            mHealthInterface.setLimit(sanitizedValue);
        }
        return sanitizedValue;
    }

    protected void setSetting(final int chargingLimit) {
        mHealthInterface.setLimit(sanitizeValue(chargingLimit));
    }

    private int sanitizeValue(final int value) {
        final int clampedValue = Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, value));
        final int snappedOffset = Math.round((clampedValue - MIN_LIMIT) / (float) STEP_SIZE)
                * STEP_SIZE;
        return MIN_LIMIT + snappedOffset;
    }

    private String formatPercentage(final int value) {
        return value + "%";
    }
}
