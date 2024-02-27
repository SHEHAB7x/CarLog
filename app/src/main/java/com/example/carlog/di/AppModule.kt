package com.example.carlog.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "Replace with the original url"
    /*@Provides
        @Singleton
        fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager {
            return BluetoothManager(context)
        }

        @Singleton
        @Provides
        fun provideOBDRepository(bluetoothSocket: BluetoothSocket): IRepo {
            return Repo(bluetoothSocket)
        }
    */
}