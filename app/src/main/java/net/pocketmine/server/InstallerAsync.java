package net.pocketmine.server;

import android.widget.TextView;
import com.nao20010128nao.PM_Metroid.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InstallerAsync extends android.os.AsyncTask<Void, String, Void> {

	Boolean fromAssets;
	Integer fromWhichAct;
	String orgLoc, toLoc;
	InstallActivity ctx;
	TextView tv_install_exec;

	@Override
	protected Void doInBackground(Void... arg0) {

		// floc = ServerUtils.getAppDirectory() + "/";
		// sdloc = ServerUtils.getDataDirectory() + "/";
		publishProgress(ctx.getString(R.string.delete_previous));
		delete(new File(toLoc));
		if (extract(fromAssets, toLoc, orgLoc)) {
			// if(extract(sdloc, "sd_data.zip")){
			publishProgress("ok");
			return null;
			// }
		}
		publishProgress("error");

		return null;
	}

	protected Boolean extract(Boolean isAsset, String loc, String zipName) {
		try {

			// dirChecker("");
			InputStream in;
			if(isAsset) {
				in = ctx.getAssets().open(zipName);
			}else{
				in = new FileInputStream(zipName);
			}
			ZipInputStream zin = new ZipInputStream(in);
			ZipEntry ze = null;

			while ((ze = zin.getNextEntry()) != null) {

				if (ze.isDirectory()) {
					// dirChecker(ze.getName());
				} else {
					java.io.File f = new java.io.File(loc, ze.getName());
					f = new java.io.File(f.getParent());
					if (!f.isDirectory())
						f.mkdirs();

					FileOutputStream fout = new FileOutputStream(loc
							+ ze.getName());

					publishProgress(ctx.getString(R.string.extracting) + ze.getName());

					byte[] buffer = new byte[4096 * 10];
					int length = 0;
					while ((length = zin.read(buffer)) != -1) {

						fout.write(buffer, 0, length);

					}

					zin.closeEntry();
					fout.close();
				}

			}

			zin.close();

			return true;

		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);

		String text = ctx.getString(R.string.error);
		tv_install_exec.setVisibility(1);
		if (values[0].equals("error"))
			text = ctx.getString(R.string.bin_error);
		else if (values[0].equals("ok")) {
			text = ctx.getString(R.string.bin_installed);
			HomeActivity.btn_runServer.setEnabled(true);
			if (fromWhichAct == 0) {
				InstallActivity ia = (InstallActivity) ctx;
				ia.contiuneInstall();
			}
		} else
			text = values[0];

		tv_install_exec.setText(text);
		HomeActivity.isStarted = false;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		ctx.installing=false;
	}

	public void delete(File f) {
		if(!f.exists())return;
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File file : files) {
				delete(file);
			}
		}
		f.delete();
	}
}
