package org.kreal.hkshare

import android.app.DialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.kreal.hkshare.configure.AppPreference
import org.kreal.hkshare.configure.Config
import org.kreal.hkshare.configure.Rout
import org.kreal.widget.filepickdialog.FilePickDialogFragment

/**
 * Created by lthee on 2018/3/18.
 *
 */
class RoutDialogFragment : DialogFragment(), View.OnClickListener, View.OnFocusChangeListener {

    override fun onFocusChange(view: View?, focus: Boolean) {
        if (focus)
            FilePickDialogFragment().apply {
                type = FilePickDialogFragment.DIRECTORY_CHOOSE
                setListener {
                    fileEdit.setText(it[0].path)
                }
            }.show(fragmentManager, "filePick")
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cancel_button -> dialog.cancel()
            R.id.ok_button -> {
                if (routEdit.text.toString().contains('/')) {
                    Toast.makeText(activity, "Rout Key can't have '/'", Toast.LENGTH_SHORT).show()
                } else {
                    val set: MutableSet<String> = mutableSetOf()
                    set.addAll(preference.getCustomRout())
                    set.add(routEdit.text.toString() + " " + fileEdit.text.toString() + " " + "true")
                    preference.setCustomRout(set)
                    config?.let {
                        val newRout = Rout(routEdit.text.toString(), fileEdit.text.toString(), true)
                        rout?.also { rout ->
                            if (newRout.key == rout.key && newRout.file == rout.file)
                                return@let
                            else {
                                it.delete(rout)
                                it.add(rout)
                            }
                        } ?: it.add(newRout)


                    }
                    dismiss()
                }
            }
        }
    }

    fun configure(config: Config, rout: Rout? = null) {
        this.config = config
        this.rout = rout
    }

    private var config: Config? = null
    private var rout: Rout? = null
    private lateinit var cancelButton: Button
    private lateinit var okButton: Button
    private lateinit var routEdit: EditText
    private lateinit var fileEdit: EditText
    private lateinit var preference: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preference = AppPreference(activity)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.rout_info_dialog, container, false)
        cancelButton = view.findViewById(R.id.cancel_button)
        okButton = view.findViewById(R.id.ok_button)
        routEdit = view.findViewById(R.id.editText)
        fileEdit = view.findViewById(R.id.editText2)

        rout?.let {
            routEdit.setText(it.key)
            fileEdit.setText(it.file)
        }

        okButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)
        fileEdit.onFocusChangeListener = this
        return view
    }
}