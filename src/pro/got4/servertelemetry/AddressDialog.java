package pro.got4.servertelemetry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

// Диалог для установки адреса сервера.
public class AddressDialog extends DialogFragment implements OnClickListener {

	private String data[];

	public interface AddressDialogListener {
		public void onAddressDialogItemSelected(AddressDialog addressDialog,
				DialogInterface dialog, int which);
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
				.setTitle(getResources().getString(R.string.addressDialogTitle))
				.setItems(data, this).setCancelable(true);

		return adb.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		((AddressDialogListener) getTargetFragment())
				.onAddressDialogItemSelected(this, dialog, which);
	}

	public void setData(String[] data) {
		this.data = data;
	}

	public String[] getData() {
		return data;
	}
}