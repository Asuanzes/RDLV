package es.tetexe.rdlv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.sromku.simple.fb.Permissions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebook.OnLoginListener;
import com.sromku.simple.fb.SimpleFacebook.OnProfileRequestListener;
import com.sromku.simple.fb.SimpleFacebook.OnPublishListener;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Feed;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.entities.Profile;

/*
 * Esta clase se encargará de mostrar los datos 
 * de los lugares, titulo, descripcion e imagen
 * recibe los datos de la activity que abre la consulta
 * y los pasa a la activity editar si es necesario
 */

public class MostrarLugar extends Activity {

	/*
	 * Definimos las variables miembro que usara esta clase
	 */
	protected static final String TAG = MostrarLugar.class.getName();

	public SimpleFacebook mSimpleFacebook;
	TextView titulo;
	ImageView fotoLugar;
	TextView descripLugar;
	TextView fechaLugar;
	TextView fecha;
	Cursor c;

	@SuppressWarnings("unused")
	private static String lat, lon, idmarker, idMarker;
	private int id, idRetorno;
	private static String location, name, idFb;
	private static Photo photo;

	// Asyntask
	final Handler handle = new Handler();

	// Recuperar datos facebook
	protected void profileThread() {
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handle.post(profile);
			}
		};
		t.start();
	}

	final Runnable profile = new Runnable() {

		@Override
		public void run() {
			if (idFb == null) {
				getProfile();
				toast("Obteniendo datos de su perfil de Facebook");
				toast("Completado");
			}

		}
	};

	// Compartir lugar
	protected void shareThread() {
		Thread tr = new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handle.post(share);
			}
		};
		tr.start();
	}

	final Runnable share = new Runnable() {

		@Override
		public void run() {
			sharePhoto();
		}
	};

	// Compartir App
	protected void shareAppThread() {
		Thread tr = new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handle.post(shareApp);
			}
		};
		tr.start();
	}

	final Runnable shareApp = new Runnable() {

		@Override
		public void run() {
			shareApp();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Permissions[] permissions = new Permissions[] {
				Permissions.USER_PHOTOS, Permissions.EMAIL,
				Permissions.PUBLISH_ACTION, Permissions.PUBLISH_STREAM,
				Permissions.USER_CHECKINS, Permissions.USER_LOCATION,
				Permissions.FRIENDS_LOCATION, Permissions.EMAIL,
				Permissions.USER_BIRTHDAY, Permissions.USER_ABOUT_ME };
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
				.setAppId("602826683129330").setNamespace("rosadelosvientos")
				.setPermissions(permissions).build();
		SimpleFacebook.setConfiguration(configuration);

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
		setContentView(R.layout.lugar_detalle_);

		// Inicializamos los componentes
		titulo = (TextView) findViewById(R.id.tituloDetalle);
		fotoLugar = (ImageView) findViewById(R.id.foto_lugar);
		descripLugar = (TextView) findViewById(R.id.descrip_lugar);
		fecha = (TextView) findViewById(R.id.fecha_lugar);
		// Personalizando la fuente
		Typeface fuente = Typeface.createFromAsset(getAssets(),
				"TravelingTypewriter.ttf");
		titulo.setTypeface(fuente);
		descripLugar.setTypeface(fuente);
		fecha.setTypeface(fuente);
		/*
		 * Inicializamos el ContentProvider definido en la aplicación con los
		 * campos que vamos a ncesitar en esta clase haciendo referencia a la
		 * URI e instanciando el ContentResolver y el cursor para recorrer la
		 * base de datos.
		 */
		String[] projection = new String[] { ActivityContentProvider._ID,
				ActivityContentProvider.TITULO,
				ActivityContentProvider.DESCRIPCION,
				ActivityContentProvider.LATITUDE,
				ActivityContentProvider.LONGITUDE,
				ActivityContentProvider.FOTO, ActivityContentProvider.FECHA };

		ContentResolver cont = getContentResolver();
		Uri lugares = ActivityContentProvider.CONTENT_URI;
		/*
		 * Recorremos la base de datos con el cursor y establecemos su posición
		 * en el primer elemento contenido en la misma
		 */
		c = cont.query(lugares, projection, null, null, null);
		c.moveToFirst();

		// Recuperamos los parámetros que nos pasan las activity
		Bundle extras = getIntent().getExtras();
		id = extras.getInt("id");
		Intent intent = getIntent();

		if (intent.getExtras().containsKey("ok")) {
			/*
			 * Si la activity origen es el listView recuperamos la posición del
			 * elemento en el View y movemos el cursor a esa posición en el
			 * provider, posteriormente mostramos los datos en la vista
			 */
			if (getIntent().getExtras().get("ok").equals("list")) {
				/*
				 * Establecemos una flag de retorno de datos
				 */
				idRetorno = 101;
				if (null != c) {

					c.moveToPosition(id);

					titulo.setText(c.getString(c.getColumnIndex("titulo")));
					descripLugar.setText(c.getString(c
							.getColumnIndex("descripcion")));
					fecha.setText(c.getString(c.getColumnIndex("fecha")));
					String colFoto = c.getString(c.getColumnIndex("foto"));

					if (colFoto != null) {

						Bitmap resizedBitmap = DecodeImagen
								.decodeSampledBitmapFromFile(colFoto, 600, 600);

						fotoLugar.setAdjustViewBounds(true);
						fotoLugar.setImageBitmap(resizedBitmap);

					} else {

						fotoLugar.setAdjustViewBounds(true);
						fotoLugar.setImageResource(R.drawable.paisaje);

					}

				}
				/*
				 * Si la activity origen es el mapa recuperamos la id del
				 * elemento del snippet del marker y la buscamos en el provider,
				 * posteriormente mostramos los datos en la vista
				 */
			} else if (getIntent().getExtras().get("ok").equals("mapa")) {
				/*
				 * Establecemos una flag de retorno de datos
				 */
				idRetorno = 202;

				idmarker = extras.getString("idMark");

				c = cont.query(lugares, projection, ActivityContentProvider._ID
						+ "=" + idmarker, null, null);

				c.moveToFirst();

				idMarker = c.getString(c.getColumnIndex("_id"));

				titulo.setText(c.getString(c.getColumnIndex("titulo")));

				descripLugar.setText(c.getString(c
						.getColumnIndex("descripcion")));
				fecha.setText(c.getString(c.getColumnIndex("fecha")));
				lat = c.getString(c.getColumnIndex("latitud"));

				lon = c.getString(c.getColumnIndex("longitud"));

				String colFoto = c.getString(c.getColumnIndex("foto"));

				if (colFoto != null) {

					Bitmap resizedBitmap = DecodeImagen
							.decodeSampledBitmapFromFile(colFoto, 600, 600);

					fotoLugar.setAdjustViewBounds(true);
					fotoLugar.setImageBitmap(resizedBitmap);

				} else {

					fotoLugar.setAdjustViewBounds(true);
					fotoLugar.setImageResource(R.drawable.paisaje);

				}

			}

		}

	}// ONCREATE

	@Override
	public void onResume() {
		super.onResume();
		// FACEBOOK
		// start Facebook Login
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		// login
		mSimpleFacebook.login(onLoginListener);
		Session session = Session.getActiveSession();

		if (session.isOpened()) {
			session.getAccessToken();
			profileThread();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	// Inflando los items del menú para su utilización desde el código
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_mostrar_lugares, menu);
		return true;
	}

	// Definiendo una acción a los items del menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.shaApp:

			AlertDialog.Builder build = new Builder(MostrarLugar.this);

			build.setTitle("Gracias por compartir Rdlv");
			build.setMessage("¿Quieres compartir esta aplicación con tus amigos?");

			build.setPositiveButton("Aceptar",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							shareAppThread();
						}
					});

			build.setNegativeButton("Cancelar",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();

						}

					});
			build.create().show();

			break;
		case R.id.menu_lista:

			Intent intents = new Intent(getApplicationContext(),
					ActivityList.class);
			startActivity(intents);
			break;
		case R.id.edit:
			// Dato para que EditarActivity sepa si ha sido iniciada desde
			// mostrarLugares o desde el mapa
			int okM = 1;

			Intent i = new Intent(getApplicationContext(), EditarActivity.class);
			/*
			 * Como los datos de la id son distintos si la activity
			 * mostrarlugares se ha iniciado desde el listView (position en el
			 * listview) o el marker (id del elemento en la base de datos)
			 * establecemos una flag para cada tipo de dato obtenido y enviar el
			 * dato correcto a editarActivity
			 */
			if (idRetorno == 101) {
				i.putExtra("idRetorno", idRetorno);
				i.putExtra("id", id);
			} else if (idRetorno == 202) {
				i.putExtra("idRetorno", idRetorno);
				i.putExtra("idMark", idMarker);
			}
			i.putExtra("okInt", okM);

			startActivity(i);
			break;
		case R.id.shaFc:

			AlertDialog.Builder builder = new Builder(MostrarLugar.this);

			builder.setTitle("Publicar");
			builder.setMessage("¿Quieres publicar esta imagen en Facebook?");

			builder.setPositiveButton("Aceptar",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							shareThread();
						}
					});

			builder.setNegativeButton("Cancelar",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();

						}

					});
			builder.create().show();

			break;

		
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}

		return true;

	}

	// login listener
	OnLoginListener onLoginListener = new SimpleFacebook.OnLoginListener() {

		@Override
		public void onFail(String reason) {
			Log.w(TAG, reason);
		}

		@Override
		public void onException(Throwable throwable) {
			Log.e(TAG, "Bad thing happened", throwable);
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is
			// happening
			Log.i(TAG, "In progress");
		}

		@Override
		public void onLogin() {
			// change the state of the button or do whatever you want
			Log.i(TAG, "Logged in");
		}

		@Override
		public void onNotAcceptingPermissions() {
			Log.w(TAG, "User didn't accept read permissions");
		}

	};

	private void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	private void getProfile() {
		OnProfileRequestListener onProfileRequestListener = new SimpleFacebook.OnProfileRequestListener() {

			@Override
			public void onFail(String reason) {

				// insure that you are logged in before getting the profile
				Log.w(TAG, reason);
			}

			@Override
			public void onException(Throwable throwable) {

				Log.e(TAG, "Bad thing happened", throwable);
			}

			@Override
			public void onThinking() {

				// show progress bar or something to the user while fetching
				// profile
				Log.i(TAG, "Thinking...");
			}

			@Override
			public void onComplete(Profile profile) {

				idFb = profile.getId();
				name = profile.getName();

				try {
					if (!profile.getLocation().getId().isEmpty()) {

						location = profile.getLocation().getId();

					} else {
						location = " ";
					}
				} catch (Throwable throwable) {
					// TODO Bloque catch generado automáticamente
					throwable.printStackTrace();
				}

				Log.i(TAG, "My profile id = " + idFb);
				Log.i(TAG, "Location1 = " + location);

				// String location =
				// profile.getLocation().getId().toString();

			}

		};
		mSimpleFacebook.getProfile(onProfileRequestListener);
	}

	private void sharePhoto() {

		// create publish listener
		OnPublishListener onPublishListener = new SimpleFacebook.OnPublishListener() {

			@Override
			public void onFail(String reason) {
				// insure that you are logged in before publishing
				Log.w(TAG, reason);
			}

			@Override
			public void onException(Throwable throwable) {
				Log.e(TAG, "Bad thing happened", throwable);
			}

			@Override
			public void onThinking() {
				// show progress bar or something to the user while
				// publishing
				Log.i(TAG, "In progress");
				toast("En progreso... " + "Publicando en tu muro " + name);
			}

			@Override
			public void onComplete(String id) {
				Log.i(TAG, "Published successfully. id = " + id);
				toast("Tu foto se ha publicado" + " " + name);
			}
		};

		// PUBLICANDO UNA FOTO EN FACEBOOK
		String colFoto = c.getString(c.getColumnIndex("foto"));

		if (colFoto != null) {
			// create Photo instace and add some properties
			Bitmap resizedBitmap = DecodeImagen.decodeSampledBitmapFromFile(
					colFoto, 400, 400);
			photo = new Photo(resizedBitmap);
			String post = c.getString(this.c.getColumnIndex("titulo"))
					.toUpperCase()
					+ " "
					+ "\n"
					+ this.c.getString(this.c.getColumnIndex("descripcion"))
					+ "\n" + this.c.getString(this.c.getColumnIndex("fecha"));
			Log.i(TAG, "Location2 = " + location);
			photo.addDescription(post);

			if (location != null && location != " ") {
				photo.addPlace(location);
			}

			// photo.addPrivacy("ALL_FRIENDS"); //CONFIGURAR PARA MENU SELECCI�N

			// publish photo to app album
			mSimpleFacebook.publish(photo, onPublishListener);
		} else {

			toast("Debe añadir una foto propia para poder publicar");
			toast("Edite el lugar y elija una foto de la galería o haga una con la cámara");
		}

	}

	public void shareApp() {
		// create publish listener
		OnPublishListener onPublishListener = new SimpleFacebook.OnPublishListener() {

			@Override
			public void onFail(String reason) {
				// insure that you are logged in before publishing
				Log.w(TAG, reason);
			}

			@Override
			public void onException(Throwable throwable) {
				Log.e(TAG, "Bad thing happened", throwable);
			}

			@Override
			public void onThinking() {
				// show progress bar or something to the user while publishing
				Log.i(TAG, "In progress");
				toast("GRACIAS!!! " + name);
			}

			@Override
			public void onComplete(String postId) {
				Log.i(TAG, "Published successfully. The new post id = "
						+ postId);
				toast(name + "  Rdlv ya está en tu muro!!");
			}
		};

		// build feed
		Feed feed = new Feed.Builder()
				.setMessage("Prueba esto...")
				.setName("Rosa de los vientos, Rdlv")
				.setCaption("Publica con un toque")
				.setDescription(
						"Publicar en Facebook nunca ha sido tan fácil!! Tus lugares, tus fotos...lo que piensas")
				.setPicture(
						"http://i746.photobucket.com/albums/xx107/Alejandro_Suanzes_Otero/RDLVfB_zpsdbf1b682.png")
				.setLink(
						"https://play.google.com/store/apps/details?id=es.tetexe.rdlv&hl=es_419")
				.build();

		// publish the feed
		mSimpleFacebook.publish(feed, onPublishListener);
	}

}
