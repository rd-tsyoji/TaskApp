package jp.techacademy.takafumi.shouji.taskapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Realmの設定
        mRealm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}