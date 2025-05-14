package com.example.dreamplan.database;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthManager {
   // private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static AuthManager instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    @Inject
    public AuthManager(FirebaseAuth auth, FirebaseFirestore firestore) {
        this.mAuth = auth;
        this.db = firestore;
    }

//    public static synchronized AuthManager getInstance() {
//        if (instance == null) {
//            instance = new AuthManager();
//        }
//        return instance;
//    }

//    public void registerUser(String email, String password, AuthCallback callback) {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        callback.onSuccess(mAuth.getCurrentUser());
//                    } else {
//                        callback.onFailure(task.getException());
//                    }
//                });
//    }
//
//    public void loginUser(String email, String password, AuthCallback callback) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null) {
//                            initializeFirestoreUser(user.getUid(), user.getEmail()); // Add this
//                        }
//                        callback.onSuccess(user);
//                    } else {
//                        callback.onFailure(task.getException());
//                    }
//                });
//    }
//
//    public boolean isUserLoggedIn() {
//        return mAuth.getCurrentUser() != null;
//    }

    public void logout() {
        mAuth.signOut();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public void initializeFirestoreUser(String userId, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Only create defaults for new users
                        WriteBatch batch = db.batch();

                        // 1. Create user document
                        DocumentReference userRef = db.collection("users").document(userId);
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("createdAt", FieldValue.serverTimestamp());
                        batch.set(userRef, userData);

                        // 2. Create default sections
                        String[] defaultSections = {"Work", "Study", "Personal"};
                        String[] colors = {"1", "2", "3"};
                        String[] descriptions = {
                                "Your professional tasks",
                                "Learning and education",
                                "Personal life matters"
                        };

                        for (int i = 0; i < defaultSections.length; i++) {
                            DocumentReference sectionRef = userRef.collection("sections").document();
                            Map<String, Object> sectionData = new HashMap<>();
                            sectionData.put("name", defaultSections[i]);
                            sectionData.put("color", colors[i]);
                            sectionData.put("notes", descriptions[i]);
                            sectionData.put("isDefault", true);
                            sectionData.put("order", i);
                            sectionData.put("createdAt", FieldValue.serverTimestamp());
                            batch.set(sectionRef, sectionData);
                        }

                        batch.commit()
                                .addOnSuccessListener(__ -> Log.d("AuthManager", "Default sections created"))
                                .addOnFailureListener(e -> Log.e("AuthManager", "Error creating sections", e));
                    } else {
                        Log.d("AuthManager", "User already exists, skipping default section creation");
                    }
                })
                .addOnFailureListener(e -> Log.e("AuthManager", "Error checking user", e));
    }

    public interface InitCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void initializeFirestoreUser(String userId, String email, InitCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if user document already exists
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // First-time setup
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("initialSetupComplete", true);

                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        // Existing user
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

}