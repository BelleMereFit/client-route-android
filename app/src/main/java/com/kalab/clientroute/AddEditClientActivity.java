package com.kalab.clientroute;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class AddEditClientActivity extends AppCompatActivity {
    public static final String EXTRA_CLIENT_ID = "extra_client_id";
    private static final String DEFAULT_STATE = "TX";

    // Simple validator contract - avoids java.util.function which is not
    // available on Android 6.0 (API 23) without core library desugaring.
    private interface FieldValidator {
        String validate(String value); // returns an error message, or null if valid
    }

    private EditText firstNameInput, lastNameInput, phoneInput, streetInput, aptInput, cityInput, zipInput;
    private TextView firstNameError, lastNameError, phoneError, streetError, cityError, zipError;
    private Spinner stateSpinner;
    private Button startTimeButton, endTimeButton;

    private String editingId;
    private int startHour24 = -1, startMinute = 0;
    private int endHour24 = -1, endMinute = 0;

    // Scratch state used only while running validation during Save.
    private boolean formValid;
    private EditText firstInvalidField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_client);

        Toolbar toolbar = findViewById(R.id.formToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        phoneInput = findViewById(R.id.phoneInput);
        streetInput = findViewById(R.id.streetInput);
        aptInput = findViewById(R.id.aptInput);
        cityInput = findViewById(R.id.cityInput);
        stateSpinner = findViewById(R.id.stateSpinner);
        zipInput = findViewById(R.id.zipInput);
        startTimeButton = findViewById(R.id.startTimeButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        startTimeButton.setVisibility(View.GONE);
        endTimeButton.setVisibility(View.GONE);
        phoneInput.setHint("Phone number (optional)");

        firstNameError = findViewById(R.id.firstNameError);
        lastNameError = findViewById(R.id.lastNameError);
        phoneError = findViewById(R.id.phoneError);
        streetError = findViewById(R.id.streetError);
        cityError = findViewById(R.id.cityError);
        zipError = findViewById(R.id.zipError);

        setUpPhoneFormatting();
        setUpStateSpinner();
        setUpFieldValidation();

        startTimeButton.setOnClickListener(v -> pickTime(true));
        endTimeButton.setOnClickListener(v -> pickTime(false));

        findViewById(R.id.saveButton).setOnClickListener(v -> saveClient());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());

        editingId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        if (editingId != null) {
            setTitle("Edit Client");
            loadExistingClient(editingId);
        } else {
            setTitle("Add Client");
            selectStateInSpinner(DEFAULT_STATE);
            updateTimeButtonLabels();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpStateSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.us_states, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(spinnerAdapter);
    }

    private void selectStateInSpinner(String abbreviation) {
        if (abbreviation == null || abbreviation.trim().isEmpty()) abbreviation = DEFAULT_STATE;
        String[] states = getResources().getStringArray(R.array.us_states);
        int index = 0;
        for (int i = 0; i < states.length; i++) {
            if (states[i].equalsIgnoreCase(abbreviation)) {
                index = i;
                break;
            }
        }
        stateSpinner.setSelection(index);
    }

    // Auto-formats the phone number as the user types: (xxx) xxx-xxxx
    private void setUpPhoneFormatting() {
        phoneInput.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;

            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 10) digits = digits.substring(0, 10);

                StringBuilder formatted = new StringBuilder();
                int len = digits.length();
                if (len > 0) {
                    formatted.append("(").append(digits.substring(0, Math.min(3, len)));
                    if (len >= 3) formatted.append(") ");
                }
                if (len > 3) {
                    formatted.append(digits.substring(3, Math.min(6, len)));
                }
                if (len > 6) {
                    formatted.append("-").append(digits.substring(6, len));
                }

                s.replace(0, s.length(), formatted.toString());
                phoneInput.setSelection(s.length());
                editing = false;
            }
        });
    }

    // Wires up "validate on blur, clear as soon as valid" behavior for every
    // required field, showing a red outline and helper text under the field.
    private void setUpFieldValidation() {
        bindValidator(firstNameInput, firstNameError, value ->
                value.isEmpty() ? "First name is required." : null);

        bindValidator(lastNameInput, lastNameError, value ->
                value.isEmpty() ? "Last name is required." : null);

        bindValidator(phoneInput, phoneError, value ->
                !value.isEmpty() && value.replaceAll("[^0-9]", "").length() < 10 ? "Enter a complete 10-digit phone number." : null);

        bindValidator(streetInput, streetError, value ->
                value.isEmpty() ? "Street address is required." : null);

        bindValidator(cityInput, cityError, value ->
                value.isEmpty() ? "City is required." : null);

        bindValidator(zipInput, zipError, value ->
                value.isEmpty() || value.matches("\\d{5}(-\\d{4})?") ? null : "Enter a valid ZIP code (12345 or 12345-6789).");
    }

    private void bindValidator(EditText field, TextView errorView, FieldValidator validator) {
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                setFieldError(field, errorView, validator.validate(text(field)));
            }
        });
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (errorView.getVisibility() == View.VISIBLE
                        && validator.validate(s.toString().trim()) == null) {
                    setFieldError(field, errorView, null);
                }
            }
        });
    }

    private void setFieldError(EditText field, TextView errorView, String message) {
        if (message == null) {
            field.setBackgroundResource(R.drawable.bg_input);
            errorView.setVisibility(View.GONE);
        } else {
            field.setBackgroundResource(R.drawable.bg_input_error);
            errorView.setText(message);
            errorView.setVisibility(View.VISIBLE);
        }
    }

    private void check(EditText field, TextView errorView, String message) {
        setFieldError(field, errorView, message);
        if (message != null) {
            formValid = false;
            if (firstInvalidField == null) firstInvalidField = field;
        }
    }

    private void pickTime(boolean isStart) {
        int currentHour = isStart ? startHour24 : endHour24;
        int currentMinute = isStart ? startMinute : endMinute;
        int hour = currentHour >= 0 ? currentHour : 9;
        int minute = currentHour >= 0 ? currentMinute : 0;

        new TimePickerDialog(this, (view, hourOfDay, selectedMinute) -> {
            if (isStart) {
                startHour24 = hourOfDay;
                startMinute = selectedMinute;
            } else {
                endHour24 = hourOfDay;
                endMinute = selectedMinute;
            }
            updateTimeButtonLabels();
        }, hour, minute, false).show();
    }

    private void updateTimeButtonLabels() {
        startTimeButton.setText("Start Time: " + Client.formatTime(startHour24, startMinute));
        endTimeButton.setText("End Time: " + Client.formatTime(endHour24, endMinute));
    }

    private void loadExistingClient(String id) {
        List<Client> clients = ClientStorage.load(this);
        for (Client c : clients) {
            if (c.id.equals(id)) {
                firstNameInput.setText(c.firstName);
                lastNameInput.setText(c.lastName);
                phoneInput.setText(c.phone);
                streetInput.setText(c.street);
                aptInput.setText(c.apt);
                cityInput.setText(c.city);
                zipInput.setText(c.zip);
                selectStateInSpinner(c.state);
                startHour24 = c.startHour24;
                startMinute = c.startMinute;
                endHour24 = c.endHour24;
                endMinute = c.endMinute;
                updateTimeButtonLabels();
                return;
            }
        }
        // Client no longer exists; treat as a new entry.
        editingId = null;
        selectStateInSpinner(DEFAULT_STATE);
        updateTimeButtonLabels();
    }

    private void saveClient() {
        String first = text(firstNameInput);
        String last = text(lastNameInput);
        String phone = text(phoneInput);
        String street = text(streetInput);
        String apt = text(aptInput);
        String city = text(cityInput);
        String state = stateSpinner.getSelectedItem() == null ? DEFAULT_STATE : stateSpinner.getSelectedItem().toString();
        String zip = text(zipInput);

        formValid = true;
        firstInvalidField = null;

        check(firstNameInput, firstNameError, first.isEmpty() ? "First name is required." : null);
        check(lastNameInput, lastNameError, last.isEmpty() ? "Last name is required." : null);
        check(phoneInput, phoneError, !phone.isEmpty() && phone.replaceAll("[^0-9]", "").length() < 10
                ? "Enter a complete 10-digit phone number." : null);
        check(streetInput, streetError, street.isEmpty() ? "Street address is required." : null);
        check(cityInput, cityError, city.isEmpty() ? "City is required." : null);
        check(zipInput, zipError, zip.isEmpty() || zip.matches("\\d{5}(-\\d{4})?") ? null
                : "Enter a valid ZIP code (12345 or 12345-6789).");

        if (!formValid) {
            if (firstInvalidField != null) firstInvalidField.requestFocus();
            Toast.makeText(this, "Please fix the highlighted fields.", Toast.LENGTH_LONG).show();
            return;
        }

        List<Client> clients = ClientStorage.load(this);
        if (editingId == null) {
            clients.add(0, new Client(String.valueOf(System.currentTimeMillis()), first, last, phone,
                    street, apt, city, state, zip, startHour24, startMinute, endHour24, endMinute));
        } else {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id.equals(editingId)) {
                    clients.set(i, new Client(editingId, first, last, phone, street, apt, city, state, zip,
                            startHour24, startMinute, endHour24, endMinute));
                    break;
                }
            }
        }
        ClientStorage.save(this, clients);
        Toast.makeText(this, "Client saved.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String text(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
