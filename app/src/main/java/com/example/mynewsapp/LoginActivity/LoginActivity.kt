package com.example.mynewsapp.LoginActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mynewsapp.MainActivity.MainActivity
import com.example.mynewsapp.R
import com.example.mynewsapp.Utils.AlertDialogHelper.ShowNetworkerrorDialog
import com.example.mynewsapp.Utils.Constant.isNetworkAvailable
import com.example.mynewsapp.Utils.SharedPrefHelper
import com.example.mynewsapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 100
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInButton.setOnClickListener {
            if (isNetworkAvailable(this)) {
                signIn()
            } else {
                ShowNetworkerrorDialog(this)
            }


        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {

        try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Google sign in successful, authenticating with Firebase")
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        Log.e(TAG, "firebaseAuthWithGoogle::::" + account.idToken)

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.e(TAG, "firebaseAuthWithGoogle:222::::")

                    val user = firebaseAuth.currentUser
                    Log.d(TAG, "Firebase sign in success: ${user?.displayName} (${user?.email})")

                    Log.e(TAG, "Firebase sign in failed" + user?.displayName)
                    Log.e(TAG, "Firebase sign in failed" + user?.email)
                    Log.e(TAG, "Firebase sign in failed" + user?.photoUrl)

                    SharedPrefHelper.saveString("UserName", user?.displayName.toString())
                    SharedPrefHelper.saveString("Email", user?.email.toString())
                    SharedPrefHelper.saveString("ProfileImage", user?.photoUrl.toString())
                    SharedPrefHelper.saveBoolean("IsLoggedin", true)

                    // Proceed to main screen
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "Firebase sign in failed", task.exception)
                }
            }
    }
}