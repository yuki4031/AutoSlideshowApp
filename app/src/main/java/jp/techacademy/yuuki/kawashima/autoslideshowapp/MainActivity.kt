package jp.techacademy.yuuki.kawashima.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import java.util.*
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.os.Handler
import android.widget.ImageView

class MainActivity : AppCompatActivity(){

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    // タイマー用の時間のための変数
    private var mTimerSec = 0.0
    private var mHandler = Handler()

    var imageUriArray = arrayListOf<Uri>()

    //カウンターの数字
    var mCountNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        start_button.setOnClickListener{
                    if(imageUriArray.size != 0){
                        if(mTimer == null){
                            //タイマーの作成
                            mTimer =  Timer()

                            //タイマーの始動
                            mTimer!!.schedule(object : TimerTask() {
                                override fun run() {
                                    mTimerSec += 0.1
                                    mHandler.post {
                                        textView.text = String.format("%.1f", mTimerSec)
                                    }
                                }
                            }, 2000, 2000)
                        }else{
                            mTimer!!.cancel()
                            mTimer = null
                        }
                    }
                }

        next_button.setOnClickListener{
                    if(imageUriArray.size != 0){
                       mCountNum += 1
                       val num = mCountNum % imageUriArray.size
                       imageView.setImageURI(imageUriArray[num])
                    }else{
                        textView.text = String.format("写真へのアクセスを許可した後に，画像を1枚以上追加してください")
                    }
                }

        back_button.setOnClickListener{
            if(imageUriArray.size != 0){
                mCountNum -= 1
                val num = mCountNum % imageUriArray.size
                imageView.setImageURI(imageUriArray[num])
                textView.text = String.format("%d枚目を表示中", num + 1)
            }else{
                textView.text = String.format("写真へのアクセスを許可した後に，画像を1枚以上追加してください")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }
    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )
        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            } while (cursor.moveToNext())
        }
        imageView.setImageURI(imageUriArray[0])
        cursor.close()
    }
}

