package com.example.elawalu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInBtn, signUpBtn;
    private ImageButton googleSignInBtn;
    private TextView forgotPassword;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private DatabaseReference usersRef;

    private SharedPreferences sharedPreferences;

    TextInputLayout emailLoginLayout,passwordLoginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://elawalu-b2fff-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users");

        emailEditText = findViewById(R.id.emailLogin);
        passwordEditText = findViewById(R.id.passwordLogin);
        signInBtn = findViewById(R.id.button);
        signUpBtn = findViewById(R.id.button2);
        googleSignInBtn = findViewById(R.id.googleSignInBtn);
        forgotPassword = findViewById(R.id.forgotPassword);


        emailLoginLayout = findViewById(R.id.emailLoginLayout);
       passwordLoginLayout = findViewById(R.id.passwordLoginLayout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInBtn.setOnClickListener(view -> signInWithEmail());
        signUpBtn.setOnClickListener(view -> startActivity(new Intent(Login.this, SignUp.class)));
        googleSignInBtn.setOnClickListener(view -> signInWithGoogle());
        forgotPassword.setOnClickListener(view -> resetPassword());


//Session Create check
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        if(sharedPreferences.getBoolean("isLoggedIn", false)){

            startActivity(new Intent(Login.this, Home.class));
            finish();

        }

    }

    private  void saveUserSession(String userId ,String displayName){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    String email = snapshot.child("email").getValue(String.class);
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String nic = snapshot.child("nic").getValue(String.class);
                    String birthday = snapshot.child("birthday").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String city = snapshot.child("city").getValue(String.class);

                    SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("USER_ID", userId);
                    editor.putString("USER_NAME", displayName);
                    editor.putString("EMAIL", email);
                    editor.putString("FIRST_NAME", firstName);
                    editor.putString("LAST_NAME", lastName);
                    editor.putString("GENDER", gender);
                    editor.putString("PHONE", phone);
                    editor.putString("PROFILE_IMAGE", profileImageUrl);
                    editor.putString("ROLE", role);
                    editor.putString("NIC", nic);
                    editor.putString("BIRTHDAY", birthday);
                    editor.putString("ADDRESS", address);
                    editor.putString("CITY", city);

                    editor.apply();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void signInWithEmail() {
        String email = emailLoginLayout.getEditText().getText().toString().trim();
        String password = passwordLoginLayout.getEditText().getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null){
                            retrieveUserDataFromDatabase(user);
                        }

                    } else {
                        Toast.makeText(this, "Authentication failed. Check email/password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveUserDataFromDatabase(FirebaseUser user) {
        usersRef.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Toast.makeText(this, " successfully Signed in ", Toast.LENGTH_SHORT).show();
                navigateToHome();
                saveUserSession(user.getUid(),user.getDisplayName());
            } else {
                Toast.makeText(Login.this, "Please Sign Up first", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                navigateToSignUp();
            }
        });

    }


    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_LONG).show();
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
                            checkUserExistsInFirebase(user);
                        }
                    } else {
                        Toast.makeText(this, "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserExistsInFirebase(FirebaseUser user) {
        usersRef.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String profileImageUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "default_url";
                Toast.makeText(this, "Signed in as " + user.getEmail(), Toast.LENGTH_SHORT).show();
                navigateToHome();
                saveUserSession(user.getUid(),user.getDisplayName());
            } else {
                Toast.makeText(Login.this, "Please Sign Up first", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                navigateToSignUp();
            }
        });
    }


    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Send password reset email
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Step 2: Show instructions about password requirements
                        showPasswordRequirementsDialog(email);
                    } else {
                        // Don't reveal whether email exists or not (security best practice)
                        Toast.makeText(this,
                                "If this email is registered, you'll receive a reset link",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showPasswordRequirementsDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Requirements");
        builder.setMessage("When setting your new password, please ensure it contains:\n\n" +
                "• At least 7 characters\n" +
                "• 1 uppercase letter\n" +
                "• 1 number\n" +
                "• 1 special character (@$!%*?&)\n\n" +
                "Check your email for the reset link.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Optionally open email client
            openEmailClient(email);
        });
        builder.show();
    }

    private void openEmailClient(String email) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Please check your email app", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPassword(String password) {
        // Regular expression for:
        // At least 7 characters, one uppercase letter, one number, one special character
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{7,}$";
        return password.matches(passwordPattern);
    }

//database default rules
//    {
//        "rules": {
//        ".read": "auth != null",  // Allow read access to authenticated users
//                ".write": "auth != null"  // Allow write access to authenticated users
//    }
//    }
//

    private void openWebView(String url) {
        Dialog webDialog = new Dialog(this);
        webDialog.setContentView(R.layout.webview_dialog);
        webDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        webDialog.setCancelable(true);

        WebView webView = webDialog.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        webDialog.show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(Login.this, Home.class);
        // Clear the back stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private void navigateToSignUp() {
        Intent intent = new Intent(Login.this, SignUp.class);
        startActivity(intent);
        finish();
    }
}
