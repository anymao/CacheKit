package com.anymore.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anymore.cachekit.DataCache
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mDataCache by lazy {
        val dir = "$cacheDir${File.separator}cachekit"
        DataCache.Builder()
            .setDiskCacheDir(dir)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        btnSaveInt.setOnClickListener {
            mDataCache.putToDisk("int",98856)
            Timber.d("存储int成功!")
        }

        btnGetInt.setOnClickListener {
            val value = mDataCache.getFromDisk<Int>("int")
            tvResult.append("int = $value \n")
        }

        btnSaveDouble.setOnClickListener {
            val d = 16.86694
            mDataCache.putToDisk("double",d)
            Timber.d("存储double成功！")
        }
        btnGetDouble.setOnClickListener {
            val doubleValue = mDataCache.getFromDisk<Double>("double")
            tvResult.append("double=$doubleValue \n")
        }

        btnSaveBitmap.setOnClickListener {
            Completable.create {
                val bitmap = BitmapFactory.decodeStream(resources.assets.open("crayon.jpg"))
                mDataCache.putToDisk("bitmap",bitmap,System.currentTimeMillis()+15*1000)
                it.onComplete()
            }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onComplete = {
                        Timber.d("存储bitmap成功!")
                    }
                )
        }

        btnGetBitmap.setOnClickListener {
            Maybe.create<Bitmap> {
                val bitmap = mDataCache.getFromDisk<Bitmap>("bitmap")
                if (bitmap != null){
                    it.onSuccess(bitmap)
                }else{
                    it.onComplete()
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        ivResult.setImageBitmap(it)
                    },onComplete = {
                        ivResult.setImageBitmap(null)
                        Timber.d("bitmap 已经过期!")
                        Toast.makeText(this,"bitmap 已经过期!",Toast.LENGTH_LONG).show()
                    }
                )
        }

        btnSaveJSONArray.setOnClickListener {
            Completable.create {
                val jsonArray = JSONArray()
                jsonArray.put(0,"abc")
                jsonArray.put(1,"www")
                jsonArray.put(2,"xyz")
                mDataCache.put("jsonArray",jsonArray,System.currentTimeMillis()+10*1000)
                it.onComplete()
            }.subscribeOn(Schedulers.io())
                .subscribeBy(
                    onError = {
                        Timber.e(it)
                    },
                    onComplete = {
                        Timber.d("jsonArray 已经存储!")
                    }
                )
        }

        btnGetJSONArray.setOnClickListener {
            Maybe.create<JSONArray> {
                val jsonArray = mDataCache.get<JSONArray>("jsonArray")
                if (jsonArray != null){
                    it.onSuccess(jsonArray)
                }
                it.onComplete()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onError = {
                        Timber.e(it)
                    },
                    onSuccess = {
                        tvResult.append("$it \n")
                    },
                    onComplete = {
                        Timber.d("jsonArray 已经过期!")
                        Toast.makeText(this,"jsonArray 已经过期!",Toast.LENGTH_LONG).show()
                    }
                )
        }
    }
}
