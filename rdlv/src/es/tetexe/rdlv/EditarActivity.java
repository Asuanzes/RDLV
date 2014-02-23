package es.tetexe.rdlv;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Esta clase permite al usuario crear nuevos lugares y
 * editar los detalles de los ya creados, imagen, titulo y 
 * descripción.
 */
public class EditarActivity extends Activity {

	/*
	 * Definimos las variables miembro que usara esta clase
	 */
	private static final int REQUEST_CAMERA = 1000;
	private static final int REQUEST_GALLERY = 2000;

	private static String rutaGaleria;
	private ImageView img;
	private TextView title;
	private TextView descrip;
	private EditText titl;
	private EditText desc;
	private long id;
	Intent photoPickerIntent;
	Intent takePictureIntent;
	Bitmap resizedBitmap;

	/*
	 * Inicializamos el ContentProvider definido en la aplicación con los campos
	 * que vamos a ncesitar en esta clase haciendo referencia a la URI e
	 * instanciando el ContentResolver y el cursor para recorrer la base de
	 * datos.
	 */
	String[] projection = new String[] { ActivityContentProvider._ID,
			ActivityContentProvider.TITULO,
			ActivityContentProvider.DESCRIPCION,
			ActivityContentProvider.LATITUDE,
			ActivityContentProvider.LONGITUDE, ActivityContentProvider.FOTO,
			ActivityContentProvider.FECHA };
	Uri lugares = ActivityContentProvider.CONTENT_URI;
	ContentResolver cont;
	Cursor c;

	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Personalizacion de la ActionBar

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
		// Instanciamos un Intent para recuperar datos de otras activity
		Intent intent = getIntent();

		// Comprobando el origen de los datos

		/*
		 * Si la llamada se realiza desde la activity Mostralugares definimos el
		 * layout que contiene los componentes para eliminar y actualizar los
		 * datos de los lugares
		 */
		if (getIntent().getExtras().get("okInt").equals(1)) {

			setContentView(R.layout.delete_save_lugar);

			// Inicializamos los componentes
			int _id = getIntent().getExtras().getInt("id");
			title = (TextView) findViewById(R.id.titulo);
			descrip = (TextView) findViewById(R.id.descrip);
			titl = (EditText) findViewById(R.id.edit_titulo);
			desc = (EditText) findViewById(R.id.edit_descrip);
			img = (ImageView) findViewById(R.id.image);
			Button btnSave = (Button) findViewById(R.id.btnSave);
			Button btnDelete = (Button) findViewById(R.id.btnDelete);

			//Definiendo una fuente desde la carpeta assets/fonts
			Typeface fuente = Typeface.createFromAsset(this.getAssets(),
					"fonts/TravelingTypewriter.ttf");

			title.setTypeface(fuente);
			descrip.setTypeface(fuente);
			btnDelete.setTypeface(fuente);
			btnSave.setTypeface(fuente);

			/*
			 * Recorremos la base de datos con el cursor y establecemos su
			 * posición en el primer elemento contenido en la misma
			 */
			cont = getContentResolver();
			c = cont.query(lugares, projection, null, null, null);

			if (c.moveToFirst()) {
				/*
				 * Recuperamos la flag para determinar si la vista
				 * mostrarlugares ha sido accedido desde la Lista de lugares o
				 * desde un marcador en el mapa
				 */
				if (getIntent().getExtras().get("idRetorno").equals(101)) {
					/*
					 * Si ha sido accedido desde la lista recuperamos la
					 * posición del elemento en el ListView
					 */
					c.moveToPosition(_id);
					/*
					 * Recuperamos la _id del elemento mostrado
					 */
					id = c.getLong(c.getColumnIndex("_id"));
					// Mostaramos el contenido del elemento pulsado
					String titulo = c.getString(c.getColumnIndex("titulo"));
					String descripcion = c.getString(c
							.getColumnIndex("descripcion"));
					String imagen = c.getString(c.getColumnIndex("foto"));

					titl.setText(titulo);
					desc.setText(descripcion);
					if (imagen != null) {

						resizedBitmap = DecodeImagen
								.decodeSampledBitmapFromFile(imagen, 400, 400);

						img.setAdjustViewBounds(true);
						img.setImageBitmap(resizedBitmap);

					} else {

						img.setAdjustViewBounds(true);
						img.setImageResource(R.drawable.paisaje);

					}
					
					/*
					 * Recuperamos la flag para determinar si la vista
					 * mostrarlugares ha sido accedido desde la Lista de lugares
					 * o desde un marcador en el mapa
					 */
					
				} else if (getIntent().getExtras().get("idRetorno").equals(202)) {
					/*
					 * Si ha sido accedido desde una vantana de información de
					 * un marker recuperamos la id del elemento que hemos
					 * establecido en el snippet. Recorremos con el cursor la
					 * base de datos para recuperar la selección de dicho
					 * element y colocamos el cursor en el primer elemento.
					 */
					String idmarker = intent.getStringExtra("idMark");

					c = cont.query(lugares, projection,
							ActivityContentProvider._ID + "=" + idmarker, null,
							null);

					c.moveToFirst();

					// Recuperamos el id del elemento y los datos de la base
					// necesarios y los inicializamos

					id = c.getLong(c.getColumnIndex("_id"));

					String titulo = c.getString(c.getColumnIndex("titulo"));
					String descripcion = c.getString(c
							.getColumnIndex("descripcion"));
					@SuppressWarnings("unused")
					String lat = c.getString(c.getColumnIndex("latitud"));

					@SuppressWarnings("unused")
					String lon = c.getString(c.getColumnIndex("longitud"));
					String imagen = c.getString(c.getColumnIndex("foto"));

					titl.setText(titulo);
					desc.setText(descripcion);
					if (imagen != null) {

						resizedBitmap = DecodeImagen
								.decodeSampledBitmapFromFile(imagen, 400, 400);

						img.setAdjustViewBounds(true);
						img.setImageBitmap(resizedBitmap);
					}
					
				}
				/*
				 * Establecemos los métodos necesarios para acceder a la galería
				 * de imágenes o camara del dispositivo. Se accede a la
				 * selección de origen de imagen mediante un AlertDialog con dos
				 * botones, camara y galería.
				 */
				img.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						AlertDialog.Builder builder = new Builder(
								EditarActivity.this);

						builder.setTitle("Editar imagen");
						builder.setMessage("Elija una opción");

						builder.setPositiveButton("Cámara",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										try {
											Intent takePictureIntent = new Intent(
													MediaStore.ACTION_IMAGE_CAPTURE);

											File f;
											/*
											 * Definimos la direccion de la
											 * nueva imagen
											 */
											f = createImageFile();
											takePictureIntent.putExtra(
													MediaStore.EXTRA_OUTPUT,
													Uri.fromFile(f));
											startActivityForResult(
													takePictureIntent,
													REQUEST_CAMERA);

										} catch (IOException e) {

											Toast.makeText(
													EditarActivity.this,
													"Error al acceder a la SDcard",
													Toast.LENGTH_SHORT).show();
										}

										dialog.dismiss();
									}
								});

						builder.setNegativeButton("Galería",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										Intent photoPickerIntent = new Intent(
												Intent.ACTION_PICK,
												android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
										photoPickerIntent.setType("image/*");
										startActivityForResult(
												photoPickerIntent,
												REQUEST_GALLERY);

									}

								});
						builder.create().show();

					}
				});
				/*
				 * Cuando se pulsa en los botones eliminar o guardar mostramos
				 * un dialog que nos solicita la confirmación o cancelación de
				 * dicha acción
				 * 
				 * METODO ELIMINAR DESHABILITADO 
				 */
				 btnDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(
								EditarActivity.this);
						dialog.setMessage(R.string.seguro)
								.setCancelable(true)
								.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												/*
												 * Eliminamos de la base de
												 * datos el elemento mostrado en
												 * la vista si pulsamos en
												 * aceptar, si no se cierra el
												 * dialog y ocntinuamos con la
												 * edición
												 */
				
												cont.delete(lugares, "_id=?",
														new String[] { String
																.valueOf(id) });
												c.requery();
												
												
												Intent intent = new Intent(
														getApplicationContext(),
														ActivityList.class);
												startActivity(intent);
												
											}

										})
								.setNegativeButton(R.string.cancelar,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.cancel();

											}
										});
						dialog.show();
					}
				});
						
				
				btnSave.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(
								EditarActivity.this);
						dialog.setMessage(R.string.guardar)
								.setCancelable(false)
								.setPositiveButton(R.string.ok,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												/*
												 * Actulizamos el lugar con los
												 * nuevos datos introducidos
												 */
												ContentValues values = new ContentValues();
												values.put("titulo", titl
														.getText().toString());
												values.put("descripcion", desc
														.getText().toString());

												if (rutaGaleria != null
														&& !rutaGaleria
																.equals("")) {
													values.put(
															"foto",
															getRealPathFromURI(Uri
																	.parse(rutaGaleria)));
												}
												cont.update(lugares, values,
														"_id=?",
														new String[] { String
																.valueOf(id) });
												
												
												/*
												 * Finalizamos la edición y
												 * mostramos la activity Lista
												 * Lugares
												 */
												
												Intent intent = new Intent(
														getApplicationContext(),
														ActivityList.class);
												startActivity(intent);

											}
										})
								.setNegativeButton(R.string.cancelar,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.cancel();

											}
										});
						dialog.show();
					}
				});

			}

			/*
			 * Si la llamada se realiza desde la activity Mapa definimos el
			 * layout que contiene los componentes para crear un nuevo lugar
			 */
		} else if (getIntent().getExtras().get("okInt").equals(0)) {
			/*
			 * Personalización e inicialización de los componentes
			 */
			setContentView(R.layout.crear_lugar);

			title = (TextView) findViewById(R.id.titulo);
			descrip = (TextView) findViewById(R.id.descrip);
			titl = (EditText) findViewById(R.id.edit_titulo);
			desc = (EditText) findViewById(R.id.edit_descrip);
			img = (ImageView) findViewById(R.id.image);

			Button btnCrear = (Button) findViewById(R.id.btnCrear);

			//Definiendo una fuente desde la carpeta assets/fonts
			Typeface fuente = Typeface.createFromAsset(this.getAssets(),
					"fonts/TravelingTypewriter.ttf");
			title.setTypeface(fuente);
			descrip.setTypeface(fuente);
			btnCrear.setTypeface(fuente);
			/*
			 * Establecemos los métodos necesarios para acceder a la galería de
			 * imágenes o camara del dispositivo. Se accede a la selección de
			 * origen de imagen mediante un AlertDialog con dos botones, camara
			 * y galería.
			 */
			img.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					AlertDialog.Builder builder = new Builder(
							EditarActivity.this);

					builder.setTitle("Editar imagen");
					builder.setMessage("Elija una opción");

					builder.setPositiveButton("Cámara",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									try {
										takePictureIntent = new Intent(
												MediaStore.ACTION_IMAGE_CAPTURE);

										File f;
										f = createImageFile();
										takePictureIntent.putExtra(
												MediaStore.EXTRA_OUTPUT,
												Uri.fromFile(f));
										startActivityForResult(
												takePictureIntent,
												REQUEST_CAMERA);

									} catch (IOException e) {

										Toast.makeText(EditarActivity.this,
												"Error al acceder a la SDcard",
												Toast.LENGTH_SHORT).show();
									}

									dialog.dismiss();
								}
							});

					builder.setNegativeButton("Galería",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									photoPickerIntent = new Intent(
											Intent.ACTION_PICK,
											android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
									photoPickerIntent.setType("image/*");
									startActivityForResult(photoPickerIntent,
											REQUEST_GALLERY);

								}

							});
					builder.create().show();

				}
			});

			btnCrear.setOnClickListener(new OnClickListener() {

				// Recuperamos los datos pasados por la activity map
				int addMyLoc = getIntent().getExtras().getInt("addMyLoc");

				@Override
				public void onClick(View v) {
					/*
					 * Si el usuario quiere definir un lugar en su localización
					 * la activity nos pasa los datos correspondientes a su
					 * latitud y longitud actual, esto se establece al pulsar el
					 * boton correspondiente en la action bar o en una parte del
					 * mapa.
					 */
					if (addMyLoc == 2) {
						/*
						 * Si la activity editar la lanza el boton mi
						 * localizacion de la activity mapa recibimos los datos
						 * de la localización actual que recupera el método
						 * crearNuevoLugar();
						 */

						crearNuevoLugar();
						
					} else if (addMyLoc != 2) {
						/*
						 * Si la activity editar la lanza una pulsación en el
						 * mapa recibimos los datos de localizacion de la
						 * pulsacion realizada que recupera el método
						 * crearNuevoLugar();
						 */

						crearNuevoLugar();
						
					}

				}
			});

		}

	}

	/*
	 * Metodo que recupera los datos de la localizacion del nuevo lugar de la
	 * activity mapa, inserta el nuevo lugar en la base de datos y finalmente
	 * abre la activity lista
	 */

	@SuppressWarnings("deprecation")
	public void crearNuevoLugar() {

		Calendar calendar = Calendar.getInstance();

		Float latitud = getIntent().getExtras().getFloat("lat");
		Float longitud = getIntent().getExtras().getFloat("long");
		ContentValues values = new ContentValues();

		values.put("latitud", latitud.toString());
		values.put("longitud", longitud.toString());
		values.put("titulo", titl.getText().toString());
		values.put("descripcion", desc.getText().toString());
		values.put("fecha", calendar.getTime().toLocaleString()
				.substring(0, 10));

		if (resizedBitmap != null) {

			values.put("foto", rutaGaleria);

		} else {
			img.setImageResource(R.drawable.paisaje);
		}
		getContentResolver()
				.insert(ActivityContentProvider.CONTENT_URI, values);
		
		Toast.makeText(EditarActivity.this, "Lugar creado con Éxito!!",
				Toast.LENGTH_SHORT).show();

		Intent intents = new Intent(getApplicationContext(),
 ActivityList.class);

		startActivity(intents);
		finish();

	}

	/*
	 * Definimos la acción de los iconos de la action bar, en este caso es un
	 * callback a la activity principal, pulsando en el icono de la aplicacion,
	 * definido en el manifest.
	 */
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

	/*
	 * Este metodo recupera la vesión de android y establece los metodos para
	 * recuperar, crear y redimensionar las imagenes dependiendo de la version
	 * recuperada.
	 */

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {

			int screenWidth = 400;

			int screenHeight = 400;

			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {

				Display display = getWindowManager().getDefaultDisplay();

				screenWidth = display.getWidth();
				screenHeight = display.getHeight();

			} else {
				Point size = new Point();

				getWindowManager().getDefaultDisplay().getRealSize(size);

				screenWidth = size.x;
				screenHeight = size.y;

			}

			if (requestCode == REQUEST_CAMERA) {

				resizedBitmap = DecodeImagen.decodeSampledBitmapFromFile(
						rutaGaleria, screenWidth, screenHeight);

				img.setAdjustViewBounds(true);
				img.setImageBitmap(resizedBitmap);

			} else if (requestCode == REQUEST_GALLERY) {

				Uri selectedImage = data.getData();

				rutaGaleria = getRealPathFromURI(selectedImage);

				resizedBitmap = DecodeImagen.decodeSampledBitmapFromFile(
						rutaGaleria, screenWidth, screenHeight);

				img.setAdjustViewBounds(true);
				img.setImageBitmap(resizedBitmap);

			}
		}

	}

	/*
	 * Recuperamos la URI para acceder a las imágenes de la galería
	 */
	private String getRealPathFromURI(Uri contenUri) {

		Cursor c = getContentResolver()
				.query(contenUri, null, null, null,
				null);

		if (c == null) {

			return contenUri.getPath();

		} else {

			c.moveToFirst();
			int idx = c
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

			return c.getString(idx);
		}

	}

	/*
	 * Metodo que establece la ruta de la nueva imagen creada
	 */
	private File createImageFile() throws IOException {

		String time = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());

		String imageFileName = "img_" + time + ".jpg";
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/rdlv/");
		dir.mkdirs();
		File imageFile = new File(dir, imageFileName);

		rutaGaleria = imageFile.getAbsolutePath();

		return imageFile;
	}

}
