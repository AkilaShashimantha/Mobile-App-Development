package com.example.elawalu;

import android.app.Dialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInBtn, signUpBtn;
    private ImageButton googleSignInBtn;
    private TextView forgotPassword;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private DatabaseReference usersRef;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInBtn.setOnClickListener(view -> signInWithEmail());
        signUpBtn.setOnClickListener(view -> startActivity(new Intent(Login.this, SignUp.class)));
        googleSignInBtn.setOnClickListener(view -> signInWithGoogle());
        forgotPassword.setOnClickListener(view -> resetPassword());

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


    }

    private void signInWithEmail() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();
                        navigateToHome(mAuth.getCurrentUser().getDisplayName(),"");
                    } else {
                        Toast.makeText(this, "Authentication failed. Check email/password", Toast.LENGTH_SHORT).show();
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
                navigateToHome(user.getDisplayName(), profileImageUrl);
            } else {
                Toast.makeText(Login.this, "Please Sign Up first", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                navigateToSignUp(user);
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

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset link sent to your email", Toast.LENGTH_LONG).show();

                        // Open WebView dialog
                        openWebView("https://accounts.google.com/ServiceLogin?service=mail");
                    } else {
                        Toast.makeText(this, "Failed to send reset email. Check your email address.", Toast.LENGTH_LONG).show();
                    }
                });
    }

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

    private void navigateToHome(String userName, String profileImageUrl) {
        Intent intent = new Intent(Login.this, Home.class);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_IMAGE", profileImageUrl);
        startActivity(intent);
        finish();
    }


    private void navigateToSignUp(FirebaseUser user) {
        Intent intent = new Intent(Login.this, SignUp.class);
        intent.putExtra("USER_NAME", user.getDisplayName());
        intent.putExtra("USER_EMAIL", user.getEmail());
        startActivity(intent);
        finish();
    }
}
