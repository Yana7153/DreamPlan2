package com.example.dreamplan.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {

    @Provides
    @Singleton
    FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }
}
