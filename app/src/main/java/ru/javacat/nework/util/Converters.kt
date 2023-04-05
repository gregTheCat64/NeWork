package ru.javacat.nework.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.javacat.nework.data.dto.response.UserPreview

class Converters {
    //    @TypeConverter
//    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)
//    @TypeConverter
//    fun fromAttachmentType(value: AttachmentType) = value.name
    private val typeToken = object : TypeToken<List<Long>>() {}.type
    private val mapToken = object : TypeToken<Map<Long, UserPreview>>() {}.type
    @TypeConverter
    fun convertListToJSON(list: List<Long>): String = Gson().toJson(list)
    @TypeConverter
    fun convertJSONToList(json: String) = Gson().fromJson<List<Long>>(json, typeToken)
    @TypeConverter
    fun convertMapToJSON(map: Map<Long,UserPreview>): String = Gson().toJson(map)
    @TypeConverter
    fun convertJSONToMap(json: String) = Gson().fromJson<Map<Long, UserPreview>>(json, mapToken)

}