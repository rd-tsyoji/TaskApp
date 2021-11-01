package jp.techacademy.takafumi.shouji.taskapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import jp.techacademy.takafumi.shouji.taskapp.databinding.ActivityCreateCategoryBinding


class CreateCategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateCategoryBinding

    /**
     * 決定ボタンが押されたときの処理
     */
    private val mOnDoneClickListener = View.OnClickListener {
        if (addCategory())
            finish()
    }

    /**
     * キャンセルボタンが押されたときの処理
     */
    private val mOnCancelClickListener = View.OnClickListener {
        finish()
    }

    /**
     * カテゴリを追加する処理
     */
    private fun addCategory(): Boolean {
        // Realmに保存
        val newCategoryName = binding.categoryEditText.text.toString()
        val existsCategory =
            mRealm.where(Category::class.java).equalTo("name", newCategoryName).findAll()
        if (existsCategory.size > 0) {
            val toastMessage = "既に登録されているカテゴリです"
            val toast: Toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG)
            toast.show()
            return false
        }

        mRealm.beginTransaction()
        val newCategory = Category()
        val categoryList = mRealm.where(Category::class.java).findAll()
        val identifier: Int =
            if (categoryList.max("id") != null) {
                categoryList.max("id")!!.toInt() + 1
            } else {
                0
            }
        newCategory.id = identifier
        newCategory.name = newCategoryName

        mRealm.copyToRealmOrUpdate(newCategory)
        mRealm.commitTransaction()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        binding.doneButton.setOnClickListener(mOnDoneClickListener)
        binding.cancelButton.setOnClickListener(mOnCancelClickListener)
    }
}