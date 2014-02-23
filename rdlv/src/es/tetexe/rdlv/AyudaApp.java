package es.tetexe.rdlv;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Clase que establece una activity de ayuda al usuario
 */
public class AyudaApp extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ayuda_app);

		// Modificando el aspecto de la ActionBar

		int actionBarTitleId = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		if (actionBarTitleId > 0) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			TextView title = (TextView) findViewById(actionBarTitleId);
			if (title != null) {
				//Definiendo una fuente desde la carpeta assets/fonts
				Typeface fuente = Typeface.createFromAsset(this.getAssets(),
						"fonts/perigord.ttf");
				title.setTypeface(fuente);
				title.setTextColor(Color.BLACK);
			}
		}

		ImageView img1 = (ImageView) findViewById(R.id.img1);
		ImageView img2 = (ImageView) findViewById(R.id.img2);
		ImageView img3 = (ImageView) findViewById(R.id.img3);
		ImageView img4 = (ImageView) findViewById(R.id.img4);

		img1.setImageResource(android.R.drawable.ic_menu_add);
		img2.setImageResource(R.drawable.device_access_location_found);
		img3.setImageResource(R.drawable.content_edit);
		img4.setImageResource(R.drawable.collections_view_as_list);

		TextView titulo1 = (TextView) findViewById(R.id.titulo_ayuda1);
		TextView titulo2 = (TextView) findViewById(R.id.titulo_ayuda2);
		TextView titulo3 = (TextView) findViewById(R.id.titulo_ayuda3);
		TextView titulo4 = (TextView) findViewById(R.id.titulo_ayuda4);

		TextView texto1 = (TextView) findViewById(R.id.texto_ayuda1);
		TextView texto2 = (TextView) findViewById(R.id.texto_ayuda2);
		TextView texto3 = (TextView) findViewById(R.id.texto_ayuda3);
		TextView texto4 = (TextView) findViewById(R.id.texto_ayuda4);
		TextView texto5 = (TextView) findViewById(R.id.texto_ayuda5);

		//Definiendo una fuente desde la carpeta assets/fonts
		Typeface fonTitle = Typeface.createFromAsset(this.getAssets(),
				"fonts/TravelingTypewriter.ttf");
		titulo1.setTypeface(fonTitle);
		titulo2.setTypeface(fonTitle);
		titulo3.setTypeface(fonTitle);
		titulo4.setTypeface(fonTitle);
		texto1.setTypeface(fonTitle);
		texto2.setTypeface(fonTitle);
		texto3.setTypeface(fonTitle);
		texto4.setTypeface(fonTitle);
		texto5.setTypeface(fonTitle);

		titulo1.setText("Añadir un lugar");
		titulo2.setText("Añadir un lugar en mi localización");
		titulo3.setText("Editar un lugar");
		titulo4.setText("Acceder a la lista");

		texto1.setText("Puedes añadir un nuevo lugar desde la lista pulsando en este icono de la barra de acción.");
		texto2.setText("Añade un nuevo lugar en tu localización actual en la vista mapa pulsando en este icono, también puedes "
				+ "añadir un lugar en otra localización pulsando en la parte del mapa que desees.");
		texto3.setText("Desde el detalle del lugar puedes editar el nombre, descripción e imagen pulsando en este icono.");
		texto4.setText("Puedes acceder a la lista de tus lugares pulsando en este icono, accesible desde el detalle del lugar y desde el mapa."
				+ " Pulsando en un lugar de la lista accederás a la "
				+ "vista en detalle del lugar, también accedes a esta vista detalle pulsando en la ventana de información del marcador en el mapa. ");
		texto5.setText("\n ¡¡PARA VERSIONES DE ANDROID NO COMPATIBLES CON LA BARRA DE ACCIÓN PUEDES ACCEDER A ESTAS CARACTERÍSTICAS DESDE LA TECLA MENÚ DEL DISPOSITIVO!!");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return true;

	}

}
