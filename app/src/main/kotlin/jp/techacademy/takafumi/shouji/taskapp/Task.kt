package jp.techacademy.takafumi.shouji.taskapp

import java.io.Serializable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * タスク エンティティクラス
 */
open class Task : RealmObject(), Serializable {
    // タイトル
    var title: String = ""

    // カテゴリ
    var category: Category? = Category()

    // 内容
    var contents: String = ""

    // 日時
    var date: Date = Date()

    // idをプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}