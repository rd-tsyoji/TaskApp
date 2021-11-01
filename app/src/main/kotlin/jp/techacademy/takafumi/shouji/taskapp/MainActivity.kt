package jp.techacademy.takafumi.shouji.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import jp.techacademy.takafumi.shouji.taskapp.databinding.ActivityMainBinding
import java.util.*

const val EXTRA_TASK = "jp.techacademy.takafumi.shouji.taskapp.TASK"
const val NO_CATEGORY = "カテゴリ無し"

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mTaskAdapter: TaskAdapter
    private lateinit var taskRealmResults: RealmResults<Task>
    private lateinit var spinner: Spinner
    private val mRealmListener = RealmChangeListener<Realm> { reloadListView() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchButton.setOnClickListener{
            val selectedCategory = spinner.selectedItem as String
            if (selectedCategory == NO_CATEGORY) {
                reloadListView()
            } else {
                searchTaskByCategory(selectedCategory)
            }
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm.addChangeListener(mRealmListener)

        spinner = findViewById<Spinner>(R.id.spinner)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this)

        // ListViewをタップしたときの処理
        binding.listView1.setOnItemClickListener { parent, view, position, id ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        binding.listView1.setOnItemLongClickListener { parent, view, position, id ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                // Realmからデータを削除
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()
                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                // アラームを解除
                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * ListViewをリロード
     */
    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
        showTaskList()
        reloadSpinner()
    }

    /**
     * カテゴリ検索処理
     */
    private fun searchTaskByCategory(queryString: String) {
        // カテゴリで検索を実施
        taskRealmResults =
            mRealm.where(Task::class.java).equalTo("category.name", queryString).findAll()
                .sort("date", Sort.DESCENDING)
        showTaskList()
    }

    /**
     * タスク一覧表示処理
     */
    private fun showTaskList() {
        // 検索結果(リロード時は全て)を、TaskListとしてセットする
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        binding.listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
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
}