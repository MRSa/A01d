package net.osdn.gokigen.a01d.liveview
import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.preference.PreferenceManager
import net.osdn.gokigen.a01d.R
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class StoreImage(private val context: Context, private val dumpLog : Boolean = false) : IStoreImage
{
    private val TAG = toString()
    private val FILENAME_FORMAT = "yyyyMMdd_HHmmss"

    override fun doStore(bitmapToStore: Bitmap)
    {
        try
        {
            // 保存処理(プログレスダイアログ（「保存中...」）を表示

            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            val isLocalLocation  = preference.getBoolean(
                IPreferencePropertyAccessor.SAVE_LOCAL_LOCATION,
                IPreferencePropertyAccessor.SAVE_LOCAL_LOCATION_DEFAULT_VALUE
            )
            if (isLocalLocation)
            {
                saveImageLocal(bitmapToStore)
            }
            else
            {
                saveImageExternal(bitmapToStore)
            }

            // 保存処理(プログレスダイアログ（「保存中...」）を削除
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
        }
    }

    private fun prepareLocalOutputDirectory(): File
    {
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mediaDir?.mkdirs()
        return (if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir)
    }

    /**
     * ビットマップイメージをファイルに出力する
     *
     * @param targetImage  出力するビットマップイメージ
     */
    private fun saveImageLocal(targetImage: Bitmap)
    {
        try
        {
            val fileName = "L" + SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
            val photoFile = File(prepareLocalOutputDirectory(), fileName)
            val outputStream = FileOutputStream(photoFile)
            targetImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun getExternalOutputDirectory(): File
    {
        val directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path + "/" + context.getString(R.string.app_name2) + "/"
        val target = File(directoryPath)
        try
        {
            target.mkdirs()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        if (dumpLog)
        {
            Log.v(TAG, "  ----- RECORD Directory PATH : $directoryPath -----")
        }
        return (target)
    }

    /**
     * ビットマップイメージを外部ストレージのファイルに出力する
     *
     * @param targetImage  出力するビットマップイメージ
     */
    @Suppress("DEPRECATION")
    private fun saveImageExternal(targetImage: Bitmap)
    {
        try
        {
            if (!isExternalStorageWritable())
            {
                saveImageLocal(targetImage)
                return
            }

            val outputDir = getExternalOutputDirectory()
            val resolver = context.contentResolver
            val mimeType = "image/jpeg"
            //val now = System.currentTimeMillis()
            val path = Environment.DIRECTORY_DCIM + File.separator + context.getString(R.string.app_name2)
            val fileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Calendar.getInstance().time) + ".jpg"

            val extStorageUri : Uri
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, path)
                values.put(MediaStore.Images.Media.IS_PENDING, true)
                extStorageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            else
            {
                values.put(MediaStore.Images.Media.DATA, outputDir.absolutePath + File.separator + fileName)
                extStorageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val imageUri = resolver.insert(extStorageUri, values)
            if (imageUri != null)
            {
                resolver.update(imageUri, values, null, null)

                ////////////////////////////////////////////////////////////////
                if (dumpLog)
                {
                    val cursor = resolver.query(imageUri, null, null, null, null)
                    DatabaseUtils.dumpCursor(cursor)
                    cursor!!.close()
                }
                ////////////////////////////////////////////////////////////////

                val outputStream = resolver.openOutputStream(imageUri)
                if (outputStream != null)
                {
                    targetImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    resolver.update(imageUri, values, null, null)

                }
            }
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
        }
    }

    private fun isExternalStorageWritable(): Boolean
    {
        return (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
    }

}
