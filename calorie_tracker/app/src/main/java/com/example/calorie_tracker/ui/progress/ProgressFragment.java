package com.example.calorie_tracker.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.content.res.ColorStateList;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.MealEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.example.calorie_tracker.util.CustomToast;

public class ProgressFragment extends Fragment {
    
    private ProgressViewModel viewModel;
    private LineChart progressChart;
    private TextView summaryTitleTextView, currentValueTextView, emptyChartTextView;
    private MaterialButtonToggleGroup timeRangeToggleGroup;
    private MaterialButton addWeightButton;
    private TabLayout tabLayout;
    
    private boolean isCalorieTab = true;
    
    private View root;
    
    private LinearLayout calorieGoalRow;
    private TextView goalValueTextView;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_progress, container, false);

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        
        initializeViews(root);
        setupListeners();

        updateUI(isCalorieTab);

        root.post(() -> {
            if (isAdded() && !isDetached()) {
                updateTimeRangeButtonStyles(R.id.weekButton);
            }
        });
        
        return root;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        System.out.println("DEBUG: ProgressFragment - onViewCreated");

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        viewModel.getDailyCalorieGoal().observe(getViewLifecycleOwner(), goal -> {
            System.out.println("DEBUG: ProgressFragment - Received calorie goal update: " + goal);
            if (goal != null && goal > 0) {
                goalValueTextView.setText(String.format(Locale.getDefault(), "%d cal", goal));
            } else {
                System.out.println("DEBUG: ProgressFragment - Invalid calorie goal received: " + goal);
                goalValueTextView.setText("0 cal");
            }
        });
    }
    
    private void initializeViews(View root) {
        tabLayout = root.findViewById(R.id.tabLayout);
        progressChart = root.findViewById(R.id.progressChart);
        summaryTitleTextView = root.findViewById(R.id.summaryTitleTextView);
        currentValueTextView = root.findViewById(R.id.currentValueTextView);
        emptyChartTextView = root.findViewById(R.id.emptyChartTextView);
        timeRangeToggleGroup = root.findViewById(R.id.timeRangeToggleGroup);
        addWeightButton = root.findViewById(R.id.addWeightButton);

        calorieGoalRow = root.findViewById(R.id.calorieGoalRow);
        goalValueTextView = root.findViewById(R.id.goalValueTextView);

        timeRangeToggleGroup.check(R.id.weekButton);

        setupChart();
    }
    
    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isCalorieTab = tab.getPosition() == 0;
                updateUI(isCalorieTab);

                if (isCalorieTab) {
                    addWeightButton.setVisibility(View.GONE);
                    calorieGoalRow.setVisibility(View.VISIBLE);
                    viewModel.getDailyCalorieGoal().observe(getViewLifecycleOwner(), goal -> {
                        if (goal != null) {
                            goalValueTextView.setText(String.format(Locale.getDefault(), "%d cal", goal));
                        }
                    });
                } else {
                    addWeightButton.setVisibility(View.VISIBLE);
                    calorieGoalRow.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        timeRangeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                updateTimeRangeButtonStyles(checkedId);
                
                ProgressViewModel.TimeRange timeRange;
                if (checkedId == R.id.weekButton) {
                    timeRange = ProgressViewModel.TimeRange.WEEK;
                } else if (checkedId == R.id.monthButton) {
                    timeRange = ProgressViewModel.TimeRange.MONTH;
                } else {
                    timeRange = ProgressViewModel.TimeRange.ALL;
                }

                viewModel.setTimeRange(timeRange);

                updateUI(isCalorieTab);

                TextView chartTitleTextView = root.findViewById(R.id.chartTitleTextView);
                String timeRangeText = getTimeRangeText();
                if (isCalorieTab) {
                    chartTitleTextView.setText(getString(R.string.progress_calories_chart_title, timeRangeText));
                } else {
                    chartTitleTextView.setText(getString(R.string.progress_weight_chart_title, timeRangeText));
                }
            }
        });

        addWeightButton.setOnClickListener(v -> showAddWeightDialog());

    }

    private void updateTimeRangeButtonStyles(int selectedButtonId) {
        if (root == null) return;

        Button weekButton = root.findViewById(R.id.weekButton);
        Button monthButton = root.findViewById(R.id.monthButton);
        Button allButton = root.findViewById(R.id.allButton);

        weekButton.setTextColor(getResources().getColor(R.color.text_secondary));
        ((com.google.android.material.button.MaterialButton) weekButton).setStrokeWidth(2);
        ((com.google.android.material.button.MaterialButton) weekButton).setStrokeColor(
            ColorStateList.valueOf(getResources().getColor(R.color.primary_light)));
        weekButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.card_background)));
        
        monthButton.setTextColor(getResources().getColor(R.color.text_secondary));
        ((com.google.android.material.button.MaterialButton) monthButton).setStrokeWidth(2);
        ((com.google.android.material.button.MaterialButton) monthButton).setStrokeColor(
            ColorStateList.valueOf(getResources().getColor(R.color.primary_light)));
        monthButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.card_background)));
        
        allButton.setTextColor(getResources().getColor(R.color.text_secondary));
        ((com.google.android.material.button.MaterialButton) allButton).setStrokeWidth(2);
        ((com.google.android.material.button.MaterialButton) allButton).setStrokeColor(
            ColorStateList.valueOf(getResources().getColor(R.color.primary_light)));
        allButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.card_background)));

        Button selectedButton = root.findViewById(selectedButtonId);
        selectedButton.setTextColor(getResources().getColor(R.color.primary));
        ((com.google.android.material.button.MaterialButton) selectedButton).setStrokeWidth(2);
        ((com.google.android.material.button.MaterialButton) selectedButton).setStrokeColor(
            ColorStateList.valueOf(getResources().getColor(R.color.primary)));

        selectedButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary_very_light)));
    }
    
    private void updateUI(boolean isCalorieTab) {
        this.isCalorieTab = isCalorieTab;

        if (root == null) return;

        TextView chartTitleTextView = root.findViewById(R.id.chartTitleTextView);
        
        if (isCalorieTab) {
            summaryTitleTextView.setText(R.string.progress_calories_summary);
            summaryTitleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calories, 0, 0, 0);

            String timeRangeText = getTimeRangeText();
            chartTitleTextView.setText(getString(R.string.progress_calories_chart_title, timeRangeText));
            chartTitleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calories, 0, 0, 0);

            observeViewModel();
        } else {
            summaryTitleTextView.setText(R.string.progress_weight_summary);
            summaryTitleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_weight, 0, 0, 0);

            String timeRangeText = getTimeRangeText();
            chartTitleTextView.setText(getString(R.string.progress_weight_chart_title, timeRangeText));
            chartTitleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_weight, 0, 0, 0);

            List<ProgressViewModel.WeightEntry> entries = viewModel.getWeightEntries();
            if (entries != null && !entries.isEmpty()) {
                updateWeightUI(entries);
            } else {
                showEmptyState(true);

                float initialWeight = viewModel.getInitialWeight();
                if (initialWeight > 0) {
                    currentValueTextView.setText(String.format(Locale.getDefault(), "%.1f kg", initialWeight));
                } else {
                    currentValueTextView.setText("-- kg");
                }
            }
        }
    }

    private String getTimeRangeText() {
        ProgressViewModel.TimeRange timeRange = viewModel.getSelectedTimeRange();
        switch (timeRange) {
            case WEEK:
                return getString(R.string.progress_last_week);
            case MONTH:
                return getString(R.string.progress_last_month);
            case ALL:
            default:
                return getString(R.string.progress_all_time);
        }
    }
    
    private void observeViewModel() {
        viewModel.getCalorieEntriesLiveData().observe(getViewLifecycleOwner(), entries -> {
            if (isCalorieTab) {
                if (entries != null && !entries.isEmpty()) {
                    updateCalorieUI(entries);
                } else {
                    showEmptyState(true);
                    currentValueTextView.setText("0 cal");
                }
            }
        });
    }
    
    private void updateCalorieUI(List<MealEntry> calorieEntries) {
        if (calorieEntries == null || calorieEntries.isEmpty()) {
            showEmptyState(true);
            return;
        }
        
        showEmptyState(false);

        Map<Date, Integer> dailyTotals = new TreeMap<>();
        
        for (MealEntry entry : calorieEntries) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(entry.getDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date normalizedDate = cal.getTime();

            int currentTotal = dailyTotals.getOrDefault(normalizedDate, 0);
            dailyTotals.put(normalizedDate, currentTotal + entry.getCaloriesConsumed());
        }
        
        if (dailyTotals.isEmpty()) {
            showEmptyState(true);
            return;
        }

        List<Map.Entry<Date, Integer>> sortedEntries = new ArrayList<>(dailyTotals.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());
        
        int currentCalories = sortedEntries.get(sortedEntries.size() - 1).getValue();

        Integer calorieGoal = viewModel.getDailyCalorieGoal().getValue();
        if (calorieGoal == null) calorieGoal = 2000; // Default value

        int percentOfGoal = (int)((float)currentCalories / calorieGoal * 100);

        currentValueTextView.setText(String.format(Locale.getDefault(), "%d cal (%d%% of goal)", currentCalories, percentOfGoal));

        updateChartData(new ArrayList<>(dailyTotals.entrySet()));
    }
    
    private void updateWeightUI(List<ProgressViewModel.WeightEntry> weightEntries) {
        if (weightEntries == null || weightEntries.isEmpty()) {
            showEmptyState(true);
            return;
        }
        
        showEmptyState(false);

        List<ProgressViewModel.WeightEntry> sortedEntries = new ArrayList<>(weightEntries);
        sortedEntries.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
        
        if (sortedEntries.isEmpty()) {
            showEmptyState(true);
            return;
        }

        float currentWeight = sortedEntries.get(sortedEntries.size() - 1).getWeight();

        currentValueTextView.setText(String.format(Locale.getDefault(), "%.1f kg", currentWeight));

        updateWeightChart(sortedEntries);
    }
    
    private void updateChartData(List<Map.Entry<Date, Integer>> dailyTotals) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> goalEntries = new ArrayList<>();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        final List<String> xAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < dailyTotals.size(); i++) {
            Map.Entry<Date, Integer> entry = dailyTotals.get(i);
            entries.add(new Entry(i, entry.getValue()));
            goalEntries.add(new Entry(i, 2000)); // Add goal line point
            xAxisLabels.add(dateFormat.format(entry.getKey()));
        }
        
        if (entries.isEmpty()) {
            showEmptyState(true);
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Calories");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.primary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_light));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setValueTextSize(10f);

        LineDataSet goalDataSet = new LineDataSet(goalEntries, "Daily Goal");
        goalDataSet.setColor(getResources().getColor(R.color.progress_warning));
        goalDataSet.setLineWidth(1.5f);
        goalDataSet.setDrawCircles(false);
        goalDataSet.setDrawValues(false);
        goalDataSet.enableDashedLine(10f, 5f, 0f);

        LineData lineData = new LineData(dataSet, goalDataSet);

        XAxis xAxis = progressChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisLabels));

        progressChart.setData(lineData);
        progressChart.invalidate();
        progressChart.setVisibility(View.VISIBLE);
    }
    
    private void updateWeightChart(List<ProgressViewModel.WeightEntry> weightEntries) {
        List<Entry> entries = new ArrayList<>();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        final List<String> xAxisLabels = new ArrayList<>();
        
        for (int i = 0; i < weightEntries.size(); i++) {
            ProgressViewModel.WeightEntry entry = weightEntries.get(i);
            entries.add(new Entry(i, entry.getWeight()));
            xAxisLabels.add(dateFormat.format(entry.getDate()));
        }
        
        if (entries.isEmpty()) {
            showEmptyState(true);
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight Progress");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.primary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_light));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);

        XAxis xAxis = progressChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisLabels));

        progressChart.setData(lineData);
        progressChart.invalidate();
        progressChart.setVisibility(View.VISIBLE);
    }
    
    private void setupChart() {
        progressChart.getDescription().setEnabled(false);
        progressChart.setTouchEnabled(true);
        progressChart.setDragEnabled(true);
        progressChart.setScaleEnabled(true);
        progressChart.setPinchZoom(true);
        progressChart.setDrawGridBackground(false);

        progressChart.setExtraBottomOffset(15f);

        XAxis xAxis = progressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);
        xAxis.setLabelRotationAngle(15f);
        xAxis.setTextSize(10f);
        xAxis.setAvoidFirstLastClipping(true);
        
        YAxis leftAxis = progressChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        
        progressChart.getAxisRight().setEnabled(false);
        progressChart.getLegend().setEnabled(false);
    }
    
    private void showEmptyState(boolean isEmpty) {
        if (root == null) return;
        
        if (isEmpty) {
            progressChart.setVisibility(View.GONE);
            emptyChartTextView.setVisibility(View.VISIBLE);

            if (isCalorieTab) {
                emptyChartTextView.setText(R.string.progress_no_calorie_data);
                if (emptyChartTextView.getText().toString().isEmpty()) {
                    emptyChartTextView.setText("No calorie data yet.\nCalorie data will appear here after you track your meals.");
                }
            } else {
                emptyChartTextView.setText(R.string.progress_no_weight_data);
                if (emptyChartTextView.getText().toString().isEmpty()) {
                    emptyChartTextView.setText("No weight data yet.\nTap the '+' button to add your weight.");
                }
            }
        } else {
            progressChart.setVisibility(View.VISIBLE);
            emptyChartTextView.setVisibility(View.GONE);
        }
    }
    
    private void showAddWeightDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = 
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_weight, null);
        builder.setView(dialogView);

        com.google.android.material.textfield.TextInputEditText weightEditText = dialogView.findViewById(R.id.weightEditText);
        com.google.android.material.textfield.TextInputEditText dateEditText = dialogView.findViewById(R.id.dateEditText);
        
        com.google.android.material.button.MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        com.google.android.material.button.MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateEditText.setText(dateFormat.format(new Date()));

        List<ProgressViewModel.WeightEntry> entries = viewModel.getWeightEntries();
        if ((entries == null || entries.isEmpty()) && viewModel.getInitialWeight() > 0) {
            weightEditText.setText(String.format(Locale.getDefault(), "%.1f", viewModel.getInitialWeight()));
        } else if (entries != null && !entries.isEmpty()) {
            ProgressViewModel.WeightEntry latestEntry = entries.get(entries.size() - 1);
            weightEditText.setText(String.format(Locale.getDefault(), "%.1f", latestEntry.getWeight()));
        }

        dateEditText.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                showMaterialDatePicker(dateEditText, dateFormat);
            } else {
                showStandardDatePicker(dateEditText, dateFormat);
            }
        });

        final androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.setOnShowListener(dialogInterface -> {
            weightEditText.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(weightEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        saveButton.setOnClickListener(v -> {
            String weightStr = weightEditText.getText().toString();
            if (weightStr.isEmpty()) {
                weightEditText.setError(getString(R.string.progress_weight_error));
                return;
            }
            
            try {
                float weight = Float.parseFloat(weightStr);

                Date date;
                try {
                    date = dateFormat.parse(dateEditText.getText().toString());

                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.setTime(date);
                    
                    Calendar todayCal = Calendar.getInstance();
                    todayCal.set(Calendar.HOUR_OF_DAY, 23);
                    todayCal.set(Calendar.MINUTE, 59);
                    todayCal.set(Calendar.SECOND, 59);
                    
                    if (selectedCal.after(todayCal)) {
                        dateEditText.setError(getString(R.string.progress_date_future_error));
                        return;
                    }
                } catch (Exception e) {
                    date = new Date();
                }

                ProgressViewModel.WeightEntry newEntry = new ProgressViewModel.WeightEntry(date, weight);

                boolean existingEntryUpdated = false;
                if (entries != null) {
                    for (ProgressViewModel.WeightEntry existingEntry : entries) {
                        Calendar existingCal = Calendar.getInstance();
                        existingCal.setTime(existingEntry.getDate());
                        
                        Calendar newCal = Calendar.getInstance();
                        newCal.setTime(date);
                        
                        if (existingCal.get(Calendar.YEAR) == newCal.get(Calendar.YEAR) && 
                            existingCal.get(Calendar.MONTH) == newCal.get(Calendar.MONTH) &&
                            existingCal.get(Calendar.DAY_OF_MONTH) == newCal.get(Calendar.DAY_OF_MONTH)) {
                            existingEntryUpdated = true;
                            break;
                        }
                    }
                }

                viewModel.addWeightEntry(newEntry);

                if (existingEntryUpdated) {
                    CustomToast.showSuccess(
                        requireContext(), 
                        getString(R.string.progress_weight_entry_updated, dateEditText.getText().toString())
                    );
                }

                updateUI(false);

                try {
                    MutableLiveData<Boolean> weightUpdatedEvent = 
                        new MutableLiveData<>();
                    weightUpdatedEvent.setValue(true);

                    androidx.fragment.app.FragmentActivity activity = getActivity();
                    if (activity != null) {
                        androidx.lifecycle.ViewModelProvider viewModelProvider = 
                            new androidx.lifecycle.ViewModelProvider(activity);
                        com.example.calorie_tracker.ui.profile.ProfileViewModel profileViewModel =
                            viewModelProvider.get(com.example.calorie_tracker.ui.profile.ProfileViewModel.class);
                        profileViewModel.notifyWeightUpdated();
                    }
                } catch (Exception e) {
                }

                dialog.dismiss();
                
            } catch (NumberFormatException e) {
                weightEditText.setError(getString(R.string.progress_weight_format_error));
            }
        });
        
        dialog.show();
    }

    private void showMaterialDatePicker(com.google.android.material.textfield.TextInputEditText dateEditText, SimpleDateFormat dateFormat) {
        try {
            com.google.android.material.datepicker.MaterialDatePicker.Builder<Long> builder =
                    com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker();
            
            builder.setTitleText(getString(R.string.progress_select_date));
            builder.setTheme(R.style.Theme_CalorieTracker_MaterialDatePicker);
            builder.setSelection(System.currentTimeMillis());

            com.google.android.material.datepicker.CalendarConstraints.Builder constraintsBuilder =
                    new com.google.android.material.datepicker.CalendarConstraints.Builder();

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            
            constraintsBuilder.setEnd(today.getTimeInMillis());
            constraintsBuilder.setValidator(
                    com.google.android.material.datepicker.DateValidatorPointBackward.now());
            
            builder.setCalendarConstraints(constraintsBuilder.build());
            
            com.google.android.material.datepicker.MaterialDatePicker<Long> picker = builder.build();
            
            picker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                dateEditText.setText(dateFormat.format(calendar.getTime()));
            });
            
            picker.show(getChildFragmentManager(), picker.toString());
        } catch (Exception e) {
            showStandardDatePicker(dateEditText, dateFormat);
        }
    }

    private void showStandardDatePicker(com.google.android.material.textfield.TextInputEditText dateEditText, SimpleDateFormat dateFormat) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(), 
                R.style.Theme_CalorieTracker_DatePicker,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    dateEditText.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        
        datePickerDialog.show();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary));
            datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary));
        }
    }
} 