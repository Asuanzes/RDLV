package es.tetexe.rdlv;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ActivityContentProvider extends ContentProvider {

	// Definimos la URI de acceso al CONTENT_PROVIDER
	// Para facilitar el código en vez de PROVIDER_NAME he llamado al objeto
	// estatico encapsulado "uri"
	public static final String PROVIDER_NAME = "es.tetexe.lugares";
	public static final String uri = "content://" + PROVIDER_NAME
			+ "/puntosmapa";
	public static final Uri CONTENT_URI = Uri.parse(uri);

	/*
	 * La primera tarea que deberá hacer nuestro content provider será
	 * interpretar la URI utilizada, para facilitar esta tarea android
	 * proporciona la clase URIMATCHER capaz de interpretar determinados
	 * patrones en una URI. Esto será útil para saber si una URI hace referencia
	 * a una tabla o a un registro concreto dentro de la tabla.
	 * 
	 * Para esto definiremos dentro de la clase interna un objeto URIMATCHER y
	 * dos nuevas constantes que indican los dos tipos de URI, acceso genérico a
	 * tabla o acceso a registro por ID
	 */
	// URIMATCHER
	private static final int PUNTOS = 1; // Flag para identificar el tipo de
											// acceso genérico
	private static final int PUNTOS_ID = 2; // Flag para identificar el tipo de
											// acceso por ID
	// Objeto de tipo URIMATCHER
	private static final UriMatcher uriMatcher;

	/*
	 * Ahora creamos las constantes con los nombres de las columnas de datos
	 * proporcionados por el CONTENPROVIDER. La columna estandar _ID (que debe
	 * contener todos los content provider) están definidas en las clase
	 * BaseColumns por lo que para añadir nuestras columnas crearemos una clase
	 * interna pública que extienda de BaseColumns y añadiremos las nuevas
	 * columnas
	 * 
	 */

	// Nombres de columnas
	public static final String _ID = "_id";
	public static final String TITULO = "titulo";
	public static final String DESCRIPCION = "descripcion";
	public static final String LATITUDE = "latitud";
	public static final String LONGITUDE = "longitud";
	public static final String FOTO = "foto";
	public static final String FECHA = "fecha";

	// Ya tenemos definidos todos los miembros necesarios para nuestro
	// content
	// AHora debemos implementar los métodos

	// Definimos varios atributos privados para almacenar el nombre de la BD, la
	// versión y la tabla a la que accederá el contentprovider
	private DBActivity puntosDb;
	private SQLiteDatabase lugaresDb;
	private static final String TABLA_PUNTOS = "puntosmapa";

	// Inicializamos el URIMATCHER
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "puntosmapa", PUNTOS);
		uriMatcher.addURI(PROVIDER_NAME, "puntosmapa/#",
				PUNTOS_ID);
	}

	/*
	 * EN el método oncreate() inicializaremos la base de datos a través de su
	 * nombre y versión utilizando para ello la clase SqliteHelper creada para
	 * nuestra BD
	 */
	@Override
	public boolean onCreate() {
		puntosDb = new DBActivity(getContext());
		lugaresDb = puntosDb.getWritableDatabase();
		return (lugaresDb == null) ? false : true;

	}

	/*
	 * Método de consulta de la base de datos, podemos realizar consultas a la
	 * base de datos definiendo los parámetros que necesitemos en nuestra
	 * consulta.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(TABLA_PUNTOS);


		if (uriMatcher.match(uri) == PUNTOS_ID) {
			sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));

		}
		if (sortOrder == null || sortOrder == "") {

			sortOrder = _ID;

		}

		Cursor c = lugaresDb.query(TABLA_PUNTOS, projection, selection,
				selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	/*
	 * Método para insertar nuevos registros en la base de datos
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Añade un nuevo lugar
		long rowID = lugaresDb.insert(TABLA_PUNTOS, "", values);
		// Si todo está bien devolvemos su URI

		if (rowID > 0) {

			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;

		}
		throw new SQLException("Error al insertar en" + uri);

	}

	/*
	 * Método para eliminar registros
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		int uriType = uriMatcher.match(uri);
		int rowsDeleted;// probar a quitar este cero

		switch (uriType) {
		case PUNTOS:
			rowsDeleted = lugaresDb.delete(TABLA_PUNTOS, selection,
					selectionArgs);
			break;

		case PUNTOS_ID:
			
			String id = uri.getLastPathSegment();
			
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = lugaresDb.delete(TABLA_PUNTOS, _ID + "=" + id,
						null);
			} else {
				rowsDeleted = lugaresDb.delete(TABLA_PUNTOS, _ID + "=" + id
						+ " and " + selection, selectionArgs);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	/*
	 * Método para actualizar resgistros existentes en la base de datos
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = uriMatcher.match(uri);
		int rowsUpdated;

		switch (uriType) {
		case PUNTOS:
			rowsUpdated = lugaresDb.update(TABLA_PUNTOS, values, selection,
					selectionArgs);
			break;

		case PUNTOS_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = lugaresDb.update(TABLA_PUNTOS, values, _ID + "="
						+ id, null);
			} else {
				rowsUpdated = lugaresDb.update(TABLA_PUNTOS, values, _ID + "="
						+ id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	/*
	 * Método para determinar el tipo de consulta recibida, si es por un solo
	 * registro o por la totalidad de los registros de la base de datos
	 */
	@Override
	public String getType(Uri uri) {

		switch (uriMatcher.match(uri)) {
		case PUNTOS:
			return "vnd.android.cursor.dir/vnd.tetexe.puntosmapa";
		case PUNTOS_ID:
			return "vnd.android.cursor.item/vnd.tetexe.puntosmapa";
		default:
			throw new IllegalArgumentException("URI no soportada" + uri);
		}
	}

}