package naya.ganj.app.roomdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
class RecentSuggestion(val query:String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}