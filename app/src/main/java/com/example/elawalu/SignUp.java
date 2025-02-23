package com.example.elawalu;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private TextView backSignIn;
    private Button signUpBtn;
    private EditText firstName, lastName, emailAddress, phoneNumber, passwordField;
    private RadioGroup radioGroupRole, radioGroupGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().hide();

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        emailAddress = findViewById(R.id.emailAddress);
        phoneNumber = findViewById(R.id.phoneNumber);
        passwordField = findViewById(R.id.password);
        signUpBtn = findViewById(R.id.signUpBtn);
        radioGroupRole = findViewById(R.id.radioGroup);
        radioGroupGender = findViewById(R.id.radioGroup2);

        signUpBtn.setOnClickListener(view -> registerUser());

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure Web Client ID is correct
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up Google Sign-Up button
        ImageButton googleSignInBtn = findViewById(R.id.googleSignUpBtn);
        googleSignInBtn.setOnClickListener(view -> signUpWithGoogle());

        EditText passwordEditText = findViewById(R.id.password);

// Set a listener for the EditText (when the icon is clicked)
        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = passwordEditText.getCompoundDrawables()[2];
                if (drawableEnd != null) {
                    // If eye icon clicked, toggle password visibility
                    if (event.getRawX() >= (passwordEditText.getRight() - drawableEnd.getBounds().width())) {
                        int selection = passwordEditText.getSelectionEnd();
                        if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            // Show password
                            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.eye_open), null);
                        } else {
                            // Hide password
                            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.eye_close), null);
                        }
                        passwordEditText.setSelection(selection); // Maintain cursor position
                        return true;
                    }
                }
            }
            return false;
        });

backSignIn = findViewById(R.id.backSignIn);
backSignIn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(SignUp.this, Login.class);
        startActivity(intent);
        finish();
    }
});

    }

    private void registerUser() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String email = emailAddress.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();

        if (selectedRoleId == -1 || selectedGenderId == -1) {
            Toast.makeText(this, "Select Role and Gender", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRole = findViewById(selectedRoleId);
        RadioButton selectedGender = findViewById(selectedGenderId);

        if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(lName) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "Invalid phone number! Start with 0 or +94 and have 9 digits after that.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be more than 6 characters and include at least one uppercase letter, one symbol, and one number!", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = selectedRole.getText().toString().trim();
        String gender = selectedGender.getText().toString().trim();

        // Register User in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            saveUserData(userId, fName, lName, email, phone, role, gender);
                        }
                    } else {
                        Toast.makeText(SignUp.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isValidPhoneNumber(String phone) {
        // Regex for Sri Lankan phone number validation
        return phone.matches("^(0\\d{9}|\\+94\\d{9})$");
    }

    // Function to validate password criteria
    private boolean isValidPassword(String password) {
        // Regular expression for:
        // At least 7 characters, one uppercase letter, one number, one special character
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{7,}$";
        return password.matches(passwordPattern);
    }

    private void saveUserData(String userId, String fName, String lName, String email, String phone, String role, String gender) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference userRef = database.getReference("Users").child(userId);

        User user = new User(fName, lName, email, phone, role, gender,"");

        userRef.setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUp.this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUp.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Error saving user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    public static class User {
        public String firstName, lastName, email, phone, role, gender, profileImageUrl;

        public User() {}

        public User(String firstName, String lastName, String email, String phone, String role, String gender, String profileImageUrl) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.gender = gender;
            this.profileImageUrl = profileImageUrl;
        }
    }


    private void signUpWithGoogle() {
        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();

        if (selectedRoleId == -1  ) {
            Toast.makeText(this, "Please Select a Role ", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Google Sign-In Failed: " + e.getStatusCode());
                Toast.makeText(this, "Google Sign-In Failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkIfUserExists(user);
                        }
                    } else {
                        Log.e("GoogleSignIn", "Firebase Authentication Failed", task.getException());
                        Toast.makeText(this, "Firebase Authentication Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkIfUserExists(FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users").child(user.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // User already exists, proceed to login
                Toast.makeText(SignUp.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUp.this, Login.class));
                finish();
            } else {
                // New user, save details
                saveGoogleUserData(user);
            }
        });
    }

    private void saveGoogleUserData(FirebaseUser user) {
        // Get role from radio button
        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
        RadioButton selectedRole = findViewById(selectedRoleId);
        String role = selectedRole != null ? selectedRole.getText().toString().trim() : "User"; // Default to "User" if no role selected

        // Extract user details from FirebaseUser object
        String fName = "";
        String lName = "";
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            String[] nameParts = user.getDisplayName().split(" ");
            fName = nameParts[0];
            if (nameParts.length > 1) {
                lName = nameParts[1];
            }
        }
        String email = user.getEmail() != null ? user.getEmail() : "N/A";
        String phone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A";

        // For Google Sign-In, gender is often not available through FirebaseUser, so it's not always retrieved
        String gender = "N/A";  // Set a default value or "N/A" if no gender available

        // Get the user profile image URL from Google Account (if available)
        String profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "default_image_url"; // Default URL if no profile picture

        // Create a new User object to store in the database
        User newUser = new User(fName, lName, email, phone, role, gender, profileImageUrl);

        // Get Firebase database reference
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users").child(user.getUid());

        // Save user data to Firebase Realtime Database
        userRef.setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUp.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUp.this, Login.class));
                        finish();
                    } else {
                        Toast.makeText(SignUp.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




}
