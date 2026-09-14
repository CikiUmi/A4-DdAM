package com.ddam_a1.gestornotas.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

//nada más lo de la fecha que se almacena como otro dato

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        // Elvis, si es null nadota. Si nooo: Agarra la UTC y el número de nanosegundos extra
        //EpochSecond trabaja con segundos, no milis
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun localTimeToTimestamp(value: LocalDateTime?): Long? {
        return value?.toEpochSecond(ZoneOffset.UTC)
    }
}