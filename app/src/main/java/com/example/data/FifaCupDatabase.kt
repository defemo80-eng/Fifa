package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TeamEntity::class,
        TournamentEntity::class,
        TournamentTeamEntity::class,
        MatchEntity::class,
        ChampionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FifaCupDatabase : RoomDatabase() {
    abstract fun fifaCupDao(): FifaCupDao

    companion object {
        @Volatile
        private var INSTANCE: FifaCupDatabase? = null

        fun getInstance(context: Context): FifaCupDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): FifaCupDatabase {
            return Room.databaseBuilder(
                appContext,
                FifaCupDatabase::class.java,
                "fifacup_sqlite.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.fifaCupDao()?.insertTeams(TeamSeedData.allTeams)
                        }
                    }
                })
                .build()
        }
    }
}
