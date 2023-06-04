package com.example.moneymaker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputFilter;
import android.text.Spanned;

import androidx.appcompat.app.AppCompatActivity;

public class PayoutFormActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText upiEditText;
    private Button submitButton;
    private  EditText pointsEditText;
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout_form);

        // Find the form input fields and submit button
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        upiEditText = findViewById(R.id.upiEditText);
        submitButton = findViewById(R.id.submitButton);
        pointsEditText = findViewById(R.id.pointsEditText);

        // Set input type to accept only numbers for points EditText
        pointsEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Apply input filter to the phone number EditText
        phoneEditText.setFilters(new InputFilter[]{new PhoneNumberInputFilter()});

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the form input values
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String phone = phoneEditText.getText().toString();
                String upiId = upiEditText.getText().toString();

                // Validate form inputs
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || upiId.isEmpty()) {
                    Toast.makeText(PayoutFormActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if the user has enough points for checkout
                    int requiredPoints = 10000;
                    int redeemedPoints;
                    try {
                        redeemedPoints = Integer.parseInt(pointsEditText.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(PayoutFormActivity.this, "Invalid points input.", Toast.LENGTH_SHORT).show();
                        return; // Exit the method if the input is invalid
                    }
                    if (redeemedPoints == requiredPoints && points >= requiredPoints) {
                        // Send email notification with form details
                        sendEmailNotification(name, email, phone, upiId);
                        Toast.makeText(PayoutFormActivity.this, "Payout request submitted.", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(PayoutFormActivity.this, "Invalid or insufficient points for checkout.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



        // Retrieve the points value from the intent extras
        points = getIntent().getIntExtra("points", 0);



    }

    public class PhoneNumberInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder filteredStringBuilder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char character = source.charAt(i);
                if (Character.isDigit(character) || character == ' ' || character == '+') {
                    filteredStringBuilder.append(character);
                }
            }
            return filteredStringBuilder.toString();
        }
    }

    private void sendEmailNotification(String name, String email, String phone, String upiId) {
        // Implement your logic to send an email notification to yourself
        // You can use libraries or APIs to send emails, such as JavaMail or a third-party email service
        // Example: send an email using JavaMail API
        // ...

        // Deduct the desired points from the user's total

        int redeemedPoints = Integer.parseInt(pointsEditText.getText().toString());
        if (redeemedPoints > points) {
            Toast.makeText(PayoutFormActivity.this, "Insufficient points.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if the user doesn't have enough points
        }
        points -= redeemedPoints;

        // Save the updated points value in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("points", points);
        editor.apply();

        // Pass the deducted points back to MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deductedPoints", redeemedPoints);
        setResult(RESULT_OK, resultIntent);

    }
}
