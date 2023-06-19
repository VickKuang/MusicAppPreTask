package cn.kwq.pretask.common

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //输出当前实例的类
        Log.i("OnCreate[+${this::class.java.name}]",javaClass.simpleName)
        ActivityList.addActivity(this)
    }

    override fun onDestroy() {
        ActivityList.removeActivity(this)
        super.onDestroy()
    }
}