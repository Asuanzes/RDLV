package es.tetexe.rdlv;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/*
 * Esta clase muestra al usuario una lista con los lugares
 * que ha creador con el titulo, descripcion y foto
 */

public class ActivityList extends Activity {
	/*
	 * Definimos las variables miembro que usara esta clase
	 */

	private ListView listview;
	public Cursor c;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Definiendo una fuente y un color para el titulo de la ActionBar

		int actionBarTitleId = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		if (actionBarTitleId > 0) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			TextView title = (TextView) findViewById(actionBarTitleId);
			if (title != null) {
				Typeface fuente = Typeface.createFromAsset(getAssets(),
						"perigord.ttf");
				title.setTypeface(fuente);
				title.setTextColor(Color.BLACK);
			}
		}

		setContentView(R.layout.lista_lugares);

		/*
		 * Inicializamos el ContentProvider definido en la aplicación con los
		 * campos que vamos a ncesitar en esta clase haciendo referencia a la
		 * URI e instanciando el ContentResolver y el cursor para recorrer la
		 * base de datos.
		 */
		String[] projection = new String[] { ActivityContentProvider._ID,
				ActivityContentProvider.TITULO,
				ActivityContentProvider.DESCRIPCION,
				ActivityContentProvider.FOTO, ActivityContentProvider.FECHA };

		ContentResolver cont = getContentResolver();
		Uri lugares = ActivityContentProvider.CONTENT_URI;

		c = cont.query(lugares, projection, null, null, null);

		String[] camposDb = new String[] { ActivityContentProvider.TITULO,
				ActivityContentProvider.DESCRIPCION,
				ActivityContentProvider.FOTO, ActivityContentProvider.FECHA};

		// Variable de los elementos definidos en el layout
		int[] camposView = new int[] { R.id.titulo, R.id.descripcion, R.id.img, R.id.fecha };

		listview = (ListView) findViewById(R.id.listview);
		// Instanciamos nuestro adaptador con los parámetros ncesarios
		// el contexto, layout, el cursor, los campos de la base de datos que
		// mostraremos y los componentes definidos en el layout
		ItemAdapter adapter = new ItemAdapter(this, R.layout.lista_config, c,
				camposDb, camposView);
		// Le pasamos el adaptador al ListView
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		// Definimos la acción al pulsar sobre un elemento de la lista
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {
				// Recuperamos la posición del item pulsado
				listview.getAdapter().getItem(position);
				Intent intent = new Intent(getApplicationContext(),
						MostrarLugar.class);
				// Pasamos la posición a la activity mostrarlugares y
				// le decimos que el dato lo envia la activity lista
				intent.putExtra("id", position);
				intent.putExtra("ok", "list");
				startActivity(intent);

			}

		});

	}

	// Inflando los items del menú para su utilización desde el código
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.principal, menu);
		return true;
	}

	// Difiniendo una acción a los items del menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_settings) {

			Intent i = new Intent(getApplicationContext(),
					MapaLugaresActivity.class);
			startActivity(i);

		}
		switch (item.getItemId()) {
		// Abre la acitivty principal
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return true;

	}

	/*
	 * Creamos nuestro Adapter personalizado que extiende de SimpleCursorAdapter
	 */

	class ItemAdapter extends SimpleCursorAdapter {

		Activity context;

		/*
		 * Creamos el constructor donde guardamos los parámetros necesarios
		 * para crear el adapter y llamamos al constructor de la clase extendida
		 */

		@SuppressWarnings("deprecation")
		ItemAdapter(Activity context, int layout, Cursor cursor,
				String[] camposDb, int[] camposView) {
			super(context, R.layout.lista_config, cursor, camposDb, camposView);
			this.context = context;

		}

		/*
		 * Método getView optimizado reutilizando los layouts que ya han sido
		 * inflados mediante el parámetro convertView. Optimizamos la
		 * obtención de la referencia a los objetos mediante la clase
		 * ViewHolder y guardandolas en el objeto item mediante el método
		 * setTag(), si el parámetro convertView no esta vacío recuperamos las
		 * referencias mediante le método getTag().
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View item = convertView;
			ViewHolder holder;
			c.moveToFirst();
			c.moveToPosition(position);

			if (item == null) {

				LayoutInflater inflater = context.getLayoutInflater();

				item = inflater.inflate(R.layout.lista_config, parent, false);
				

				holder = new ViewHolder();

				holder.next = (ImageView) item.findViewById(R.id.next);
				holder.title = (TextView) item.findViewById(R.id.titulo);
				holder.descrip = (TextView) item.findViewById(R.id.descripcion);
				holder.image = (ImageView) item.findViewById(R.id.img);
				holder.date = (TextView) item.findViewById(R.id.fecha);
				item.setTag(holder);

			} else {

				holder = (ViewHolder) item.getTag();

			}
			if (c.getString(c.getColumnIndex("titulo")).length() >= 17) {
				holder.title.setText(c.getString(c.getColumnIndex("titulo")).substring(0, c.getString(c.getColumnIndex("titulo")).lastIndexOf(" ", 13)) + "...");
			}else {
				holder.title.setText(c.getString(c.getColumnIndex("titulo")));
			}
			if (c.getString(c.getColumnIndex("descripcion")).length() >= 85) {
				holder.descrip
				.setText(c.getString(c.getColumnIndex("descripcion")).substring(0, c.getString(c.getColumnIndex("descripcion")).lastIndexOf(" ", 75)) + "...");
			}else {
				holder.descrip
				.setText(c.getString(c.getColumnIndex("descripcion")));
			}
			
			

			holder.date.setText(c.getString(c.getColumnIndex("fecha")));

			String img = c.getString(c.getColumnIndex("foto"));

			if (img != null) {

				Bitmap resizedBitmap = DecodeImagen
						.decodeSampledBitmapFromFile(img, 100, 100);
				holder.image.setAdjustViewBounds(true);
				holder.image.setImageBitmap(resizedBitmap);

			} else {

				holder.image.setAdjustViewBounds(true);
				holder.image.setImageResource(R.drawable.paisaje);

			}

			holder.next.setImageResource(R.drawable.navigation_next_item);
			Typeface fuente = Typeface.createFromAsset(getAssets(),
					"TravelingTypewriter.ttf");

			holder.title.setTypeface(fuente);
			holder.descrip.setTypeface(fuente);
			holder.date.setTypeface(fuente);

			return item;

		}

	}

	/*
	 * Clase ViewHolder conteniendo las referencias a los objetos
	 */
	static class ViewHolder {
		private ImageView image;
		private TextView title;
		private TextView descrip;
		private ImageView next;
		private TextView date;
	}

}
