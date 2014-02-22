package es.tetexe.rdlv;

import java.util.Hashtable;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * Esta activity se encargará de mostrar los markers de los lugares,
 * de establecer nuetra posición actual, de lanzar la activity
 * editar cuando se produzca un evento de pulsación en el mapa
 * o en el menu añadir mi posicion y de pasar los datos necesarios 
 * para crear un lugar nuevo.
 */

public class MapaLugaresActivity extends FragmentActivity implements
		OnMapClickListener {
	/*
	 * Definimos las variables miembro que usara esta clase
	 */

	private GoogleMap map;
	@SuppressWarnings("unused")
	private LatLng latlong, latLng;
	private Location location;
	private Criteria criteria;
	private static float longitud;
	private static float latitud;
	private Marker marker;
	private LocationListener locListener;
	private Hashtable<String, String> markers;
	Bitmap resizedBitmap;
	static String img;
	String idMark;
	CameraPosition position;
	/* Referencia al gestor de localizacion. */

	private LocationManager locationManager;
	final Handler handle = new Handler();

	protected void locationThread() {
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handle.post(locationP);
			}
		};
		t.start();
	}

	final Runnable locationP = new Runnable() {

		@Override
		public void run() {
			// Inicializamos el método de geoLocalización
			miPosicionActual();
			map.setMyLocationEnabled(true);
		}
	};
	String[] projection = new String[] { ActivityContentProvider._ID,
			ActivityContentProvider.TITULO,
			ActivityContentProvider.DESCRIPCION,
			ActivityContentProvider.LATITUDE,
			ActivityContentProvider.LONGITUDE, ActivityContentProvider.FOTO,
			ActivityContentProvider.FECHA };
	Uri lugares = ActivityContentProvider.CONTENT_URI;
	ContentResolver cont;
	Cursor c;
	/* Nombre del proveedor de localización. */

	private transient String proveedor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Cambiando el color del texto y la fuente del título de la ActionBar
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

		setContentView(R.layout.main_map);

		Button satelite = (Button) findViewById(R.id.satelite);
		Button hibrido = (Button) findViewById(R.id.hibrido);
		Button terreno = (Button) findViewById(R.id.terreno);
		Typeface fuente = Typeface.createFromAsset(getAssets(),
				"TravelingTypewriter.ttf");
		satelite.setTypeface(fuente);
		hibrido.setTypeface(fuente);
		terreno.setTypeface(fuente);
		satelite.setOnClickListener(onClickListener);
		hibrido.setOnClickListener(onClickListener);
		terreno.setOnClickListener(onClickListener);

		setUpMapIfNeeded();

		map.setOnMapClickListener(this);

		/*
		 * Inicializamos el ContentProvider definido en la aplicación con los
		 * campos que vamos a ncesitar en esta clase haciendo referencia a la
		 * URI e instanciando el ContentResolver y el cursor para recorrer la
		 * base de datos.
		 */

		cont = getContentResolver();
		c = cont.query(lugares, projection, null, null, null);
		// /////
		if (map != null) {
			map.setInfoWindowAdapter(new MyInfoWindowAdapter());

			addMarker();
			

		}
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		if (marker!=null) {
			// Definiendo la posción inicial de la cámara al iniciar el mapa
			position = new CameraPosition.Builder().target(latlong).zoom(17).bearing(60).tilt(90).build();
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			map.animateCamera(update);
		}
		
		/*
		 * Establecemos una acción para la pulsación en la ventana de
		 * información del marker, lanza la activity mostrarlugares pasandole la
		 * id establecida en el snippet
		 */

		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {

				idMark = marker.getSnippet();

				Intent intent = new Intent(getApplicationContext(),
						MostrarLugar.class);

				intent.putExtra("idMark", idMark);
				intent.putExtra("ok", "mapa");
				startActivity(intent);
				marker.showInfoWindow();
			}
		});

	}// ONCREATE

	/*
	 * El método posicionInicial realiza el posicionamiento inicial. Cuando se
	 * pulsa en el botón añadir un lugar en mi posición se activa el GPS para
	 * determinar nuestra posición.
	 */
	private void miPosicionActual() {

		// Referencia al LocationManager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Mediante la clase Criteria obtenemos el mejor provider disponible
		// y lo utilizamos en la obtención de la última posición conocida
		criteria = new Criteria();
		proveedor = locationManager.getBestProvider(criteria, true);

		locListener = new LocationListener() {
			/*
			 * Métodos para informar del estado del GPS
			 */
			@Override
			public void onProviderDisabled(String proveedor) {

				Toast.makeText(
						MapaLugaresActivity.this,
						"GPS desactivado, su localización actual puede que no este actualizada",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onProviderEnabled(String proveedor) {

				Toast.makeText(MapaLugaresActivity.this, "GPS activado",
						Toast.LENGTH_SHORT).show();

			}

			@Override
			public void onStatusChanged(String proveedor, int status,
					Bundle extras) {

				Toast.makeText(MapaLugaresActivity.this, "GPS" + status, 500)
						.show();

			}

			/*
			 * Recuperamos los datos actualizados de nuestra posición
			 */
			@Override
			public void onLocationChanged(Location location) {

				int latitud = (int) (location.getLatitude() * 1E6);
				int longitud = (int) (location.getLongitude() * 1E6);

				latLng = new LatLng(latitud, longitud);

			}
		};

		// Actualizamos los datos de la posición del dispositivo
		// mediante los datos que recibe el LocationManager provider.
		locationManager.requestLocationUpdates(
				LocationManager.PASSIVE_PROVIDER, 0L, 0f, locListener);

		// Obtenemos la última posición registrada en el provider
		location = locationManager.getLastKnownLocation(proveedor);

		if (location != null) {
			latitud = (float) location.getLatitude();
			longitud = (float) location.getLongitude();
			latLng = new LatLng(location.getLatitude(), location.getLongitude());

		} else {

			// TOAST PARA INFORMAR DE QUE NO HAY DATOS DE LA POSICIÓN ACTUAL

			Toast.makeText(MapaLugaresActivity.this,
					"No es posible acceder a los datos de su posición actual",
					Toast.LENGTH_SHORT).show();
		}

	}

	// Detenemos la recepción de nuevas actualizaciones de posición al salir
	// de
	// la activity mapa en el método onDestroy
	@Override
	protected void onDestroy() {
		locationManager.removeUpdates(locListener);
		super.onDestroy();
	}

	// Inflando los items del menú para su utilización desde el código
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_map, menu);
		return true;
	}

	// Difiniendo una acción a los items del menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_lista:
			Intent i = new Intent(getApplicationContext(), ActivityList.class);
			startActivity(i);
			break;
		case R.id.menu_addLoc:
			/*
			 * Pasamos los datos de latitud y longitud de nuestra localización
			 * actual a editaractivity
			 */
			int resultOk = 0;
			int addMyLoc = 2;
			miPosicionActual();

			Intent in = new Intent(getApplicationContext(),
					EditarActivity.class);

			in.putExtra("lat", latitud);
			in.putExtra("long", longitud);
			in.putExtra("addMyLoc", addMyLoc);
			in.putExtra("okInt", resultOk);
			startActivityForResult(in, 10);
			break;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return true;

	}

	// Verificando la disponibilidad del mapa
	private void setUpMapIfNeeded() {
		// Si el objeto no está disponible (aún no ha sido instanciado) se
		// procede
		// a recuperar una instancia del mismo
		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();

		}

	}

	/*
	 * Evento de pulsación en el mapa, recojemos los datos de la posición
	 * pulsada y se los pasamos a editarActivity
	 */
	@Override
	public void onMapClick(LatLng loc) {

		latitud = (float) loc.latitude;
		longitud = (float) loc.longitude;
		latlong = new LatLng(latitud, longitud);

		int resultOk = 0;
		Intent in = new Intent(getApplicationContext(), EditarActivity.class);
		in.putExtra("okInt", resultOk);
		in.putExtra("lat", latitud);
		in.putExtra("long", longitud);

		startActivityForResult(in, 10);

	}

	/*
	 * Menú para seleccionar el tipo de terreno para el mapa
	 */
	private final OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.hibrido:
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case R.id.terreno:
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
			case R.id.satelite:
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;

			default:
				break;
			}
		}

	};

	@SuppressWarnings("deprecation")
	public void addMarker() {

		markers = new Hashtable<String, String>();
		c.requery();

		if (c.getCount() > 0) {

			c.moveToFirst();

			do {

				latlong = new LatLng(c.getFloat(c.getColumnIndex("latitud")),
						c.getFloat(c.getColumnIndex("longitud")));
				if (c.getString(c.getColumnIndex("descripcion")).length() > 113) {

					marker = map
							.addMarker(new MarkerOptions()
									.title(this.c
											.getString(
													this.c.getColumnIndex("descripcion"))
											.substring(
													0,
													this.c.getString(
															this.c.getColumnIndex("titulo"))
															.lastIndexOf(" ",
																	105))
											+ "...")
									.snippet(
											this.c.getString(this.c
													.getColumnIndex("_id")))
									.position(this.latlong));
				} else {
					marker = map.addMarker(new MarkerOptions()
							.title(c.getString(c.getColumnIndex("descripcion")))
							.snippet(c.getString(c.getColumnIndex("_id")))
							.position(latlong));
				}

				if (c.getString(c.getColumnIndex("foto")) != null) {
					markers.put(marker.getId(),
							c.getString(c.getColumnIndex("foto")));
				}
				marker.showInfoWindow();
			} while (c.moveToNext());

		} else {
			Toast.makeText(MapaLugaresActivity.this, "Añada un nuevo lugar",
					Toast.LENGTH_SHORT).show();
		}

	}

	public class MyInfoWindowAdapter implements InfoWindowAdapter {

		private View v;

		MyInfoWindowAdapter() {
			v = getLayoutInflater().inflate(R.layout.custom_info, null);

		}

		@Override
		public View getInfoContents(Marker marker) {
			if (MapaLugaresActivity.this.marker != null
					&& MapaLugaresActivity.this.marker.isInfoWindowShown()) {
				MapaLugaresActivity.this.marker.hideInfoWindow();
				MapaLugaresActivity.this.marker.showInfoWindow();
			}

			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) {

			MapaLugaresActivity.this.marker = marker;
			String img = null;

			if (marker.getId() != null && markers != null && markers.size() > 0) {
				if (markers.get(marker.getId()) != null
						&& markers.get(marker.getId()) != null) {
					img = markers.get(marker.getId());
				}
			}
			final ImageView imgi = (ImageView) v.findViewById(R.id.imgi);

			if (img != null && !img.equalsIgnoreCase("null")
					&& !img.equalsIgnoreCase("")) {

				Bitmap resizedBitmap = DecodeImagen
						.decodeSampledBitmapFromFile(img, 100, 100);

				imgi.setImageBitmap(resizedBitmap);

			} else {
				imgi.setAdjustViewBounds(true);
				imgi.setImageResource(R.drawable.paisaje);
			}

			TextView tvTitulo = (TextView) v.findViewById(R.id.tituloi);
			tvTitulo.setText(marker.getTitle());
			// TextView tvDescr = (TextView) v.findViewById(R.id.descripcioni);
			// tvDescr.setText(marker.getSnippet());

			Typeface fuente = Typeface.createFromAsset(getAssets(),
					"TravelingTypewriter.ttf");

			tvTitulo.setTypeface(fuente);
			// tvDescr.setTypeface(fuente);

			return v;

		}

	}

	@Override
	public void onResume() {
		super.onResume();

		locationThread();
	}
}
