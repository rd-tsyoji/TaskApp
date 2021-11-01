package jp.techacademy.takafumi.shouji.taskapp

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import java.io.Serializable

/**
 * カテゴリ エンティティクラス
 */
open class Category : RealmObject(), Serializable {
    // カテゴリ名
    var name: String = ""

    @PrimaryKey
    var id: Int = 0

    @LinkingObjects("category")
    private val tasks: RealmResults<Task>? = null
}