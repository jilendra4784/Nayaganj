package naya.ganj.app.data.sidemenu.view


import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jsibbold.zoomage.ZoomageView
import com.squareup.picasso.Picasso
import naya.ganj.app.Nayaganj
import naya.ganj.app.R
import naya.ganj.app.data.sidemenu.adapter.VirtualRecyclerviewAdapter
import naya.ganj.app.data.sidemenu.repositry.SideMenuDataRepositry
import naya.ganj.app.data.sidemenu.viewmodel.SideMenuViewModelFactory
import naya.ganj.app.data.sidemenu.viewmodel.VirtualOrderViewModel
import naya.ganj.app.databinding.ActivityMyVirtualBinding
import naya.ganj.app.interfaces.OnInternetCheckListener
import naya.ganj.app.interfaces.OnitemClickListener
import naya.ganj.app.retrofit.RetrofitClient
import naya.ganj.app.utility.NetworkResult
import naya.ganj.app.utility.Utility
import java.io.IOException


class MyVirtualActivity : AppCompatActivity(), OnitemClickListener {
    lateinit var bining: ActivityMyVirtualBinding
    lateinit var viewModel: VirtualOrderViewModel
    lateinit var app: Nayaganj

    var currentPosition = 0.0
    var totalDuration = 0.0
    var mediaPlayer: MediaPlayer? = null
    lateinit var seekBar: SeekBar
    lateinit var pause: ImageView
    var current: TextView? = null
    var total: TextView? = null
    private lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bining = ActivityMyVirtualBinding.inflate(layoutInflater)
        setContentView(bining.root)

        app = applicationContext as Nayaganj
        handler = Handler(Looper.getMainLooper())
        mediaPlayer = MediaPlayer()

        bining.include12.ivBackArrow.setOnClickListener { finish() }

        if(app.user.getAppLanguage()==1){
            bining.include12.toolbarTitle.text=resources.getString(R.string.my_virtual_order_hindi)
        }else{
            bining.include12.toolbarTitle.text = "My Virtual Orders"
        }

        viewModel = ViewModelProvider(
            this,
            SideMenuViewModelFactory(SideMenuDataRepositry(RetrofitClient.instance))
        )[VirtualOrderViewModel::class.java]

    }

    override fun onResume() {
        super.onResume()
        if (Utility.isAppOnLine(this@MyVirtualActivity, object : OnInternetCheckListener {
                override fun onInternetAvailable() {
                    getVirtualOrderData()
                }

            }))
            getVirtualOrderData()
    }

    private fun getVirtualOrderData() {
        bining.rvVirtualList.layoutManager = LinearLayoutManager(this@MyVirtualActivity)
        bining.rvVirtualList.isNestedScrollingEnabled = false
        viewModel.getMyVirtualOrderList(this@MyVirtualActivity, app.user.getUserDetails()?.userId)
            .observe(this@MyVirtualActivity) { response ->
                when (response) {
                    is NetworkResult.Success -> {
                        val it = response.data!!
                        bining.rvVirtualList.adapter =
                            VirtualRecyclerviewAdapter(
                                this@MyVirtualActivity,
                                it.virtualOrdersList,
                                this@MyVirtualActivity
                            )
                    }
                    is NetworkResult.Error -> {
                        Utility.serverNotResponding(
                            this@MyVirtualActivity,
                            response.message.toString()
                        )
                    }
                }
            }
    }

    override fun onclick(position: Int, data: String) {
        if (data.contains(".mp3")) {
            playAudioPlayerDialog(data)
        } else {
            showFullscreenImage(data)
        }
    }

    private fun showFullscreenImage(data: String) {

        var alertDialog : AlertDialog?=null
        val displayRectangle = Rect()
        val window: Window = this@MyVirtualActivity.getWindow()
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val builder = AlertDialog.Builder(this@MyVirtualActivity)
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_fullscreen, viewGroup, false)

        dialogView.minimumWidth = (displayRectangle.width() * 1f).toInt()
        dialogView.minimumHeight = (displayRectangle.height() * 1f).toInt()
        val zoomableImageView2 = dialogView.findViewById(R.id.myZoomageView) as ZoomageView

        Picasso.get().load(data).resize(500,1200).into(zoomableImageView2)
        val ivClose = dialogView.findViewById(R.id.iv_close) as ImageView
        ivClose.setOnClickListener { alertDialog?.dismiss() }

        builder.setView(dialogView)
         alertDialog = builder.create()
        alertDialog.show()
    }


    private fun playAudioPlayerDialog(data: String) {
        val view = LayoutInflater.from(this)
        val deleteDialogView: View = view.inflate(R.layout.audio_dialog, null)
        val icCloseIcon = deleteDialogView.findViewById<ImageView>(R.id.iv_close_icon)

        seekBar = deleteDialogView.findViewById(R.id.seekbar)
        pause = deleteDialogView.findViewById(R.id.paush)
        current = deleteDialogView.findViewById(R.id.current)
        total = deleteDialogView.findViewById(R.id.total)

        val dialog = AlertDialog.Builder(this).create()
        val window = dialog.window
        window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(deleteDialogView)
        dialog.show()

        pause.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                pause.setImageResource(R.drawable.play_icon)
            } else {
                mediaPlayer!!.start()
                pause.setImageResource(R.drawable.pause_icon)
                setAudioProgress()
            }
        }

        icCloseIcon.setOnClickListener {
            if (mediaPlayer != null) {
                mediaPlayer!!.release()
                dialog.dismiss()
            }
        }
        if (data.isNotBlank()) {
            mediaPlayer = MediaPlayer()
            playAudio(data)
        }
    }

    private fun playAudio(audioUrl: String) {
        try {
            mediaPlayer?.setDataSource(this, Uri.parse(audioUrl))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            pause.setImageResource(R.drawable.pause_icon)
            setAudioProgress()

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setAudioProgress() {
        var isAudioFinished = false
        currentPosition = mediaPlayer!!.currentPosition.toDouble()
        totalDuration = mediaPlayer!!.duration.toDouble()
        //display the audio duration
        total?.text = timerConversion(totalDuration.toLong())
        current?.text = timerConversion(currentPosition.toLong())
        seekBar.max = totalDuration.toInt()

        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    currentPosition = mediaPlayer!!.currentPosition.toDouble()
                    current?.setText(timerConversion(currentPosition.toLong()))
                    seekBar.progress = currentPosition.toInt()

                    if (currentPosition.toInt() == totalDuration.toInt()) {
                        pause.setImageResource(R.drawable.play_icon)
                        isAudioFinished = true
                    }
                    if (!isAudioFinished)
                        handler.postDelayed(this, 1000)
                } catch (ed: IllegalStateException) {
                    ed.printStackTrace()
                }
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    fun timerConversion(value: Long): String? {

        val audioTime: String
        val dur = value.toInt()
        val hrs = dur / 3600000
        val mns = dur / 60000 % 60000
        val scs = dur % 60000 / 1000
        audioTime = if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mns, scs)
        } else {
            String.format(
                "%02d:%02d",
                mns,
                scs
            )
        }
        return audioTime
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
    }
}