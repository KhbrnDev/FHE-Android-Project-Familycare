package de.fhe.familycare.storage.core

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import de.fhe.familycare.storage.dao.*
import de.fhe.familycare.storage.enums.Gender
import de.fhe.familycare.storage.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Class for Room-database.
 */
@Database(
    entities = [FamilyMember::class,
        WeightData::class,
        FamilyMemberType::class,
        Appointment::class,
        Contact::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FamilyCareDatabase : RoomDatabase() {

    /**
     * Functions to get Dao from database
     *
     * @return the Dao object
     */
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun weightDataDao(): WeightDataDao
    abstract fun familyMemberTypeDao(): FamilyMemberTypeDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: FamilyCareDatabase? = null

        /**
         * Function to get Database
         *
         * @return The FamilyCareDatabase
         */
        fun getDatabase(context: Context): FamilyCareDatabase? = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                FamilyCareDatabase::class.java,
                "familycare-database"
            )
                .addCallback(createCallback)
                .build()

            INSTANCE = instance
            return INSTANCE
        }

        private const val NUMBER_OF_THREADS: Int = 4
        private val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(
            NUMBER_OF_THREADS
        )

        /**
         * Helper methods to ease external usage of ExecutorService
         * e.g. perform async database operations
         */
        @Throws(ExecutionException::class, InterruptedException::class)
        fun <T> executeWithReturn(task: Callable<T>?): T {
            return databaseWriteExecutor.invokeAny(Collections.singletonList(task))
        }

        fun execute(runnable: Runnable) {
            databaseWriteExecutor.execute(runnable)
        }

        private val createCallback: Callback = object : Callback() {

            /**
             * Function to be executed when database is created.
             * Deletes all existing data of all Daos.
             * Creates one FamilyMember as Example with 1000 WeightData-Objects as an example.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                Log.i(FamilyCareDatabase::class.java.name, "Database Created")
                execute {
                    val familyMemberDao = INSTANCE?.familyMemberDao()
                    familyMemberDao?.deleteAll()

                    val weightDataDao = INSTANCE?.weightDataDao()
                    weightDataDao?.deleteAll()

                    val familyMemberTypeDao = INSTANCE?.familyMemberTypeDao()
                    familyMemberTypeDao?.deleteAll()

                    val appointmentDao = INSTANCE?.appointmentDao()
                    appointmentDao?.deleteAll()

                    val contactDao = INSTANCE?.contactDao()
                    contactDao?.deleteAll()

                    // setup a familyMembeer
                    val familyMember = FamilyMember(
                        id = 1,
                        name = "Max Mustermann",
                        isCastrated = false,
                        isHuman = true,
                        birthdate = "01.01.${LocalDate.now().year - 20}",
                        gender = Gender.MALE,
                        isActive = true,
                        picturePath = null,
                        note = "Das ist Max Mustermann",
                        type = FamilyMemberType("Muster"),
                        height = 180
                    )
                    familyMemberDao?.insertUpdateFamilyMember(familyMember)

                    familyMemberTypeDao?.insertUpdateFamilyMemberType(FamilyMemberType("Muster"))

                    // 4 Appointments for example FamilyMember

                    val converter = Converters()
                    val dateLong = converter.localDateTimeToLong(LocalDateTime.now())
                    for (i in 1..4) {
                        val appointment = Appointment(
                            id = i.toLong(),
                            title = "Termin $i",
                            date_start = converter.longToLocalDateTime(dateLong?.plus(((i - 2) * 100_000))),
                            familyMemberID = familyMember.id,
                            familyMemberName = "",
                            note = "Bitte reisepass mitbringen"
                        )
                        appointmentDao?.insertUpdateAppointment(appointment)
                    }


                    // setup weight data
                    val weightData = WeightData()
                    var weight = 80F

                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    val birthdate: LocalDate = LocalDate.parse(familyMember.birthdate, formatter)
                    val today: LocalDate = LocalDate.now()
                    val weightDataDate = ChronoUnit.YEARS.between(birthdate, today)

                    for (i in 1000 downTo 0 step 1) {
                        weightData.apply {
                            this.id = 1001 - i.toLong()
                            this.date = Date.from(
                                LocalDate.now(ZoneId.systemDefault()).minusDays(i.toLong())
                                    .atStartOfDay(
                                        ZoneId.systemDefault()
                                    ).toInstant()
                            )
                            this.age = weightDataDate
                            this.familyMemberId = familyMember.id
                            this.height = 1.80F
                            this.weight = weight

                        }
                        weightDataDao?.insertUpdateWeightData(weightData)

                        var start = 1
                        var end = 5

                        if (weight > 85) {
                            end = 4
                        } else if (weight < 75) {
                            start = 2
                        }

                        weight += Random.nextInt(start, end) - 2.5F

                    }
                }
            }
        }
    }
}