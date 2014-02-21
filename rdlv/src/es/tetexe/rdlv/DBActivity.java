package es.tetexe.rdlv;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBActivity extends SQLiteOpenHelper {

	// variable asignando nombre de la bd
	private final static String DB_NAME = "puntosmapa";
	// variable asignando numero de versión de la bd
	private static int DB_VERSION = 1;

	public DBActivity(Context context) {
		super(context, DB_NAME, null, DB_VERSION);

	}

	/*
	 * Métodos para la creación de la base de datos
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		if (db.isReadOnly()) {
			db = getWritableDatabase();
		}

		db.execSQL("CREATE TABLE puntosmapa "
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "titulo TEXT, "
				+ "descripcion TEXT, " + "latitud FLOAT, " + "longitud FLOAT, "
				+ "foto TEXT, fecha LONGTEXT);");

		db.getVersion();


	}

	/*
	 * Si existe una nueva versión de la base de datos elimina la antigua y crea
	 * una nueva con los nuevos parámetros
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		oldVersion = DB_VERSION;

		db.execSQL("DROP TABLE IF EXISTS puntosmapa");

		db.execSQL(DB_NAME);
		newVersion = DB_VERSION++;
		db.getVersion();

	}

}
