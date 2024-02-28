package com.example.carlog.di

import android.content.Context
import com.example.carlog.utils.MyBluetoothManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothManager(@ApplicationContext context: Context): MyBluetoothManager {
        return MyBluetoothManager(context)
    }

}