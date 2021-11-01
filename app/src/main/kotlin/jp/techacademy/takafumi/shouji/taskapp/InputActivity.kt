package jp.techacademy.takafumi.shouji.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.get
import androidx.core.view.iterator
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import jp.techacademy.takafumi.shouji.taskapp.databinding.ActivityInputBinding
import java.util.*

/**
 * タスクの入力画面
 */
class InputActivity : BaseActivity() {

    private lateinit var binding: ActivityInputBinding
    private lateinit var spinner: Spinner
    private val mRealmListener = RealmChangeListener<Realm> { reloadSpinner() }

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    /**
     * 日付設定が押されたときの処理
     */
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = "$mYear/" + String.format(
                    "%02d",
                    mMonth + 1
                ) + "/" + String.format("%02d", mDay)
                binding.input.dateButton.text = dateString
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    /**
     * 時刻設定が押されたときの処理
     */
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                binding.input.timesButton.text = timeString
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }

    /**
     * 決定ボタンが押されたときの処理
     */
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    /**
     * キャンセルボタンが押されたときの処理
     */
    private val mOnCancelClickListener = View.OnClickListener {
        finish()
    }

    /**
     * カテゴリSpinnerをリロード
     */
    private fun reloadSpinner() {
        val resultCategories: RealmResults<Category> =
            mRealm.where(Category::class.java).findAll().sort("id", Sort.DESCENDING)
        val categoryList = mutableListOf(NO_CATEGORY)
        resultCategories.forEach { categoryList.add(it.name) }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // Realmの設定
        mRealm.addChangeListener(mRealmListener)

        spinner = findViewById<Spinner>(R.id.spinner)
        reloadSpinner()

        // UI部品の設定
        binding.input.dateButton.setOnClickListener(mOnDateClickListener)
        binding.input.timesButton.setOnClickListener(mOnTimeClickListener)
        binding.input.doneButton.setOnClickListener(mOnDoneClickListener)
        binding.input.cancelButton.setOnClickListener(mOnCancelClickListener)
        binding.input.createCategoryButton.setOnClickListener {
            val intent = Intent(this, CreateCategoryActivity::class.java)
            startActivity(intent)
        }

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            binding.input.titleEditText.setText(mTask!!.title)
            for (i in 0 until spinner.count) {
                if (spinner.getItemAtPosition(i) == mTask!!.category?.name) {
                    spinner.setSelection(i)
                    break
                }
            }
            binding.input.contentEditText.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = "$mYear/" + String.format(
                "%02d",
                mMonth + 1
            ) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            binding.input.dateButton.text = dateString
            binding.input.timesButton.text = timeString
        }
    }

    /**
     * タスクを追加する処理
     */
    private fun addTask() {
        // Realmに保存
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = binding.input.titleEditText.text.toString()
        val categoryString = spinner.selectedItem as String
        val content = binding.input.contentEditText.text.toString()

        mTask!!.title = title
        val selectCategory = mRealm.where(Category::class.java).equalTo("name", categoryString).findAll()
        mTask!!.category = if (selectCategory.size > 0) selectCategory[0] else null
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()

        // アラームをセット
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }
}