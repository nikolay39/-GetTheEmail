package app.saby.gettheemail

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class EmailDialog:DialogFragment() {
    private var title: String? = null;
    private var message: String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            message = it.getString(ARG_MESSAGE)
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder =   AlertDialog.Builder(it)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.dialog_positive_button)) { dialog, id ->
                dialog.cancel();
            };
            builder.create();
        }?: throw IllegalStateException("Activity cannot be null")
    }
    companion object {
        const val TAG = "EmailDialog"
        private const val ARG_TITLE = "argTitle"
        private const val ARG_MESSAGE = "argMessage"
        fun newInstance(title: String, message: String) = EmailDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}
