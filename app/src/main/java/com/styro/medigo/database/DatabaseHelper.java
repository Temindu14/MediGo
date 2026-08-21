package com.styro.medigo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.styro.medigo.model.CartItem;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medigo.db";
    private static final int DATABASE_VERSION = 1;

    // Cart table
    private static final String TABLE_CART = "cart";
    private static final String COL_ID = "id";
    private static final String COL_MEDICINE_ID = "medicine_id";
    private static final String COL_MEDICINE_NAME = "medicine_name";
    private static final String COL_MEDICINE_IMAGE_URL = "medicine_image_url";
    private static final String COL_PRICE = "price";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_REQUIRES_PRESCRIPTION = "requires_prescription";
    private static final String COL_PRESCRIPTION_PATH = "prescription_path";

    private static final String CREATE_CART_TABLE =
            "CREATE TABLE " + TABLE_CART + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MEDICINE_ID + " TEXT NOT NULL, " +
                    COL_MEDICINE_NAME + " TEXT NOT NULL, " +
                    COL_MEDICINE_IMAGE_URL + " TEXT, " +
                    COL_PRICE + " REAL NOT NULL, " +
                    COL_QUANTITY + " INTEGER NOT NULL, " +
                    COL_REQUIRES_PRESCRIPTION + " INTEGER DEFAULT 0, " +
                    COL_PRESCRIPTION_PATH + " TEXT" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CART_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }


    // Add item to cart or update quantity if it already exists
    public long addToCart(CartItem item) {
        SQLiteDatabase db = getWritableDatabase();

        // Check if item already exists in cart
        Cursor cursor = db.query(
                TABLE_CART,
                new String[]{COL_ID, COL_QUANTITY},
                COL_MEDICINE_ID + " = ?",
                new String[]{item.getMedicineId()},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            // Update existing item quantity
            int existingId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, existingQuantity + item.getQuantity());

            return db.update(TABLE_CART, values, COL_ID + " = ?",
                    new String[]{String.valueOf(existingId)});
        }

        cursor.close();

        // Insert new item
        ContentValues values = new ContentValues();
        values.put(COL_MEDICINE_ID, item.getMedicineId());
        values.put(COL_MEDICINE_NAME, item.getMedicineName());
        values.put(COL_MEDICINE_IMAGE_URL, item.getMedicineImageUrl());
        values.put(COL_PRICE, item.getPrice());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_REQUIRES_PRESCRIPTION, item.isRequiresPrescription() ? 1 : 0);
        values.put(COL_PRESCRIPTION_PATH, item.getPrescriptionPath());

        return db.insert(TABLE_CART, null, values);
    }

    public List<CartItem> getAllCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CART,
                null,
                null, null, null, null,
                COL_ID + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                item.setMedicineId(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDICINE_ID)));
                item.setMedicineName(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDICINE_NAME)));
                item.setMedicineImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDICINE_IMAGE_URL)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)));
                item.setRequiresPrescription(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REQUIRES_PRESCRIPTION)) == 1);
                item.setPrescriptionPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRESCRIPTION_PATH)));
                cartItems.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return cartItems;
    }

    public int updateCartItemQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_QUANTITY, newQuantity);

        return db.update(TABLE_CART, values, COL_ID + " = ?",
                new String[]{String.valueOf(itemId)});
    }

    public int removeCartItem(int itemId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CART, COL_ID + " = ?",
                new String[]{String.valueOf(itemId)});
    }

    public void clearCart() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, null, null);
    }

    public int getCartItemCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CART, null);
        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    public double getCartTotal() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_PRICE + " * " + COL_QUANTITY + ") FROM " + TABLE_CART,
                null
        );
        double total = 0.0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        return total;
    }

}