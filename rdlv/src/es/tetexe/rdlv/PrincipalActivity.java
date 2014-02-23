package es.tetexe.rdlv;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * Activity de entrada a la aplicación, define el aspecto de la ActionBar, 
 * una animación para la imagen elegida y el aspecto y funcionalidad de los
 * botones.
 */

public class PrincipalActivity extends Activity {

	protected static final String TAG = PrincipalActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		// Modificando el aspecto de la ActionBar
		int actionBarTitleId = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");

		if (actionBarTitleId > 0) {

			TextView title = (TextView) findViewById(actionBarTitleId);

			if (title != null) {
				//Definiendo una fuente desde la carpeta assets/fonts
				Typeface fuente = Typeface.createFromAsset(this.getAssets(),
						"fonts/perigord.ttf");

				title.setTypeface(fuente);
				title.setTextColor(Color.BLACK);
				title.setTextSize(35);

			}
		}
		getAppKeyHash();

		ImageView mi_app = (ImageView) findViewById(R.id.mi_app);
		mi_app.setImageResource(R.drawable.rosadelosvientostransp);

		// Definimos los botones y le pasamos al objeto creado una llamada
		// al método onClickListener
		Button btnMap = (Button) findViewById(R.id.activity_mapa);
		btnMap.setOnClickListener(onClickListener);

		Button btnLista = (Button) findViewById(R.id.activity_lista);
		btnLista.setOnClickListener(onClickListener);

		// Definiendo un tipo de fuente personalizada en los botones
		Typeface fuente = Typeface.createFromAsset(this.getAssets(),
				"fonts/TravelingTypewriter.ttf");

		btnLista.setTypeface(fuente);
		btnMap.setTypeface(fuente);

		// Definiendo una animación en la imagen de la Activity principal
		Animation rotar = AnimationUtils.loadAnimation(this, R.anim.anim_rosa);
		mi_app.startAnimation(rotar);

	}// ONCREATE
	


	// Estableciendo la funcionalidad de los botones de acceso a mapa y a lista
	// mediante el método onClikListener
	private final OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			switch (v.getId()) {
			case R.id.activity_lista:
				Intent intent = new Intent(getApplicationContext(),
						ActivityList.class);
				startActivity(intent);
				break;
			case R.id.activity_mapa:
				Intent intents = new Intent(getApplicationContext(),
						MapaLugaresActivity.class);
				startActivity(intents);
				break;

			}

		}
	};

	/*
	 * Definimos la acción de los iconos de la action bar
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_ayuda, menu);
		return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_ayuda:
			Intent intent = new Intent(getApplicationContext(), AyudaApp.class);
			startActivity(intent);

		}

		return true;

	}

	

	private void getAppKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;

				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.d("Hash key", something);
			}
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			Log.e("name not found", e1.toString());
		}

		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			Log.e("no such an algorithm", e.toString());
		} catch (Exception e) {
			Log.e("exception", e.toString());
		}

	}

}