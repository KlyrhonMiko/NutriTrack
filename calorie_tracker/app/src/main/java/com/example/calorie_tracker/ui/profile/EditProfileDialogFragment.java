package com.example.calorie_tracker.ui.profile;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.calorie_tracker.R;
import com.example.calorie_tracker.data.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileDialogFragment extends DialogFragment {

    private ProfileViewModel viewModel;
    private TextInputEditText nameEditText, ageEditText, heightEditText, weightEditText;
    private RadioGroup genderRadioGroup, goalTypeRadioGroup;
    private AutoCompleteTextView activityLevelDropdown;
    private MaterialButton saveButton, cancelButton;
    private ImageButton closeButton;

    private String originalName;
    private int originalAge;
    private String originalGender;
    private float originalHeight;
    private float originalWeight;
    private int originalActivityLevel;
    private String originalWeightGoalType;

    private Runnable onDismissListener;

    public static EditProfileDialogFragment newInstance() {
        return new EditProfileDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_profile, container, false);
        initViews(view);
        loadUserData();
        setupListeners();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.Theme_CalorieTracker_Slide);

            dialog.setOnShowListener(dialogInterface -> {
                if (getDialog() == null || !isAdded()) {
                    if (onDismissListener != null) {
                        onDismissListener.run();
                    }
                }
            });
        }
    }

    private void initViews(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        ageEditText = view.findViewById(R.id.ageEditText);
        heightEditText = view.findViewById(R.id.heightEditText);
        weightEditText = view.findViewById(R.id.weightEditText);
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup);
        goalTypeRadioGroup = view.findViewById(R.id.goalTypeRadioGroup);
        activityLevelDropdown = view.findViewById(R.id.activityLevelDropdown);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        closeButton = view.findViewById(R.id.closeButton);

        String[] activityLevels = getResources().getStringArray(R.array.activity_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, activityLevels);
        activityLevelDropdown.setAdapter(adapter);
    }

    private void loadUserData() {
        originalName = viewModel.getUserName();
        originalAge = viewModel.getUserAge();
        originalGender = viewModel.getUserGender();
        originalHeight = viewModel.getUserHeight();
        originalWeight = viewModel.getUserWeight();
        originalActivityLevel = viewModel.getUserActivityLevel();
        originalWeightGoalType = viewModel.getUserWeightGoalType();

        nameEditText.setText(originalName);
        ageEditText.setText(String.valueOf(originalAge));
        heightEditText.setText(String.valueOf(originalHeight));
        weightEditText.setText(String.valueOf(originalWeight));

        if (originalGender.equalsIgnoreCase("Male")) {
            genderRadioGroup.check(R.id.radioMale);
        } else if (originalGender.equalsIgnoreCase("Female")) {
            genderRadioGroup.check(R.id.radioFemale);
        }

        if (originalWeightGoalType.equals("Lose")) {
            goalTypeRadioGroup.check(R.id.radioLoseWeight);
        } else if (originalWeightGoalType.equals("Maintain")) {
            goalTypeRadioGroup.check(R.id.radioMaintainWeight);
        } else if (originalWeightGoalType.equals("Gain")) {
            goalTypeRadioGroup.check(R.id.radioGainWeight);
        }

        String[] activityLevels = getResources().getStringArray(R.array.activity_levels);
        if (originalActivityLevel > 0 && originalActivityLevel <= activityLevels.length) {
            activityLevelDropdown.setText(activityLevels[originalActivityLevel - 1], false);
        }
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserData();
                dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        closeButton.setOnClickListener(v -> dismiss());
    }

    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String ageString = ageEditText.getText().toString().trim();
        String heightString = heightEditText.getText().toString().trim();
        String weightString = weightEditText.getText().toString().trim();
        String activityLevel = activityLevelDropdown.getText().toString().trim();

        if (name.isEmpty() || ageString.isEmpty() || heightString.isEmpty() || weightString.isEmpty() || activityLevel.isEmpty()) {
            showError("Please fill in all fields");
            return false;
        }

        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            showError("Please select your gender");
            return false;
        }

        if (goalTypeRadioGroup.getCheckedRadioButtonId() == -1) {
            showError("Please select your weight goal");
            return false;
        }

        return true;
    }

    private void saveUserData() {
        try {
            String name = nameEditText.getText().toString().trim();
            int age = Integer.parseInt(ageEditText.getText().toString().trim());
            float height = Float.parseFloat(heightEditText.getText().toString().trim());
            float weight = Float.parseFloat(weightEditText.getText().toString().trim());

            RadioButton selectedGenderButton = getView().findViewById(genderRadioGroup.getCheckedRadioButtonId());
            String gender = selectedGenderButton.getText().toString();

            RadioButton selectedGoalButton = getView().findViewById(goalTypeRadioGroup.getCheckedRadioButtonId());
            String weightGoalType = selectedGoalButton.getText().toString();

            String[] activityLevels = getResources().getStringArray(R.array.activity_levels);
            String selectedActivity = activityLevelDropdown.getText().toString();
            int activityLevel = 1;

            for (int i = 0; i < activityLevels.length; i++) {
                if (activityLevels[i].equals(selectedActivity)) {
                    activityLevel = i + 1;
                    break;
                }
            }

            android.util.Log.d("EditProfile", "Saving user data: " + name + ", age: " + age + ", gender: " + gender);
            android.util.Log.d("EditProfile", "Height: " + height + ", weight: " + weight + ", activity: " + activityLevel + ", goal: " + weightGoalType);

            User updatedUser = new User(name, age, gender, height, weight, activityLevel, weightGoalType);

            User currentUser = viewModel.getUserData().getValue();
            if (currentUser != null) {
                updatedUser.setId(currentUser.getId());
                android.util.Log.d("EditProfile", "Updating user profile with ID: " + currentUser.getId());
                viewModel.updateUserProfile(updatedUser);
                android.util.Log.d("EditProfile", "Profile updated successfully");
            } else {
                android.util.Log.e("EditProfile", "Current user is null, cannot update profile");
                showError("Error updating profile");
            }
        } catch (Exception e) {
            android.util.Log.e("EditProfile", "Error saving profile: " + e.getMessage());
            showError("An error occurred: " + e.getMessage());
        }
    }

    private void showError(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.run();
        }
    }
} 