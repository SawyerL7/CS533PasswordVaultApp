// AccountDataContentProvider.java
// ContentProvider subclass for manipulating the app's database
package com.smd.passwordvault.sql;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.smd.passwordvault.R;
import com.smd.passwordvault.helpers.Constants;

public class AccountDataContentProvider extends ContentProvider {
   // used to access the database
   private DatabaseHelper dbHelper;

   private SharedPreferences sharedpreferences;

   private static final String TAG = "AccntDtContentProvider";

   // UriMatcher helps ContentProvider determine operation to perform
   private static final UriMatcher uriMatcher =
      new UriMatcher(UriMatcher.NO_MATCH);

   // constants used with UriMatcher to determine operation to perform
   private static final int ONE_ACCOUNT = 1; // manipulate one account
   private static final int ACCOUNTS = 2; // manipulate accounts table

   // static block to configure this ContentProvider's UriMatcher
   static {
      // Uri for Contact with the specified id (#)
      uriMatcher.addURI(DatabaseDescription.AUTHORITY,
         DatabaseDescription.AccountData.TABLE_NAME + "/#", ONE_ACCOUNT);

      // Uri for Contacts table
      uriMatcher.addURI(DatabaseDescription.AUTHORITY,
         DatabaseDescription.AccountData.TABLE_NAME, ACCOUNTS);
   }

   // called when the AccountDataContentProvider is created
   @Override
   public boolean onCreate() {
      // create the AddressBookDatabaseHelper
      dbHelper = new DatabaseHelper(getContext());
      sharedpreferences = getContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
      return true; // ContentProvider successfully created
   }

   // required method: Not used in this app, so we return null
   @Override
   public String getType(Uri uri) {
      return null;
   }

   // query the database
   @Override
   public Cursor query(Uri uri, String[] projection,
      String selection, String[] selectionArgs, String sortOrder) {

      // create SQLiteQueryBuilder for querying accounts table
      SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
      queryBuilder.setTables(DatabaseDescription.AccountData.TABLE_NAME);

      switch (uriMatcher.match(uri)) {
         case ONE_ACCOUNT: // account with specified id will be selected
            queryBuilder.appendWhere(
               DatabaseDescription.AccountData.COLUMN_ACCOUNT_ID + "=" + uri.getLastPathSegment());
            break;
         case ACCOUNTS: // all accounts will be selected
            // filter accounts for the current user
            int loggedInUserIdFromSession = sharedpreferences.getInt(Constants.USER_ID_KEY, 0);
            Log.v(TAG, "********* query - loggedInUserIdFromSession:" + loggedInUserIdFromSession);
            queryBuilder.appendWhere(
                    DatabaseDescription.AccountData.COLUMN_USER_ID + "=" + loggedInUserIdFromSession);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_query_uri) + uri);
      }

      // execute the query to select one or all accounts
      Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
         projection, selection, selectionArgs, null, null, sortOrder);

      // configure to watch for content changes
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
      return cursor;
   }

   // insert a new account in the database
   @Override
   public Uri insert(Uri uri, ContentValues values) {
      Uri newAccountUri = null;

      int loggedInUserIdFromSession = sharedpreferences.getInt(Constants.USER_ID_KEY, 0);
      Log.v(TAG, "********* insert - loggedInUserIdFromSession:" + loggedInUserIdFromSession);
      values.put(DatabaseDescription.AccountData.COLUMN_USER_ID, loggedInUserIdFromSession);
      Log.v(TAG, "Inserting an AccountData entry with user id");

      switch (uriMatcher.match(uri)) {
         case ACCOUNTS:
            // insert the new account--success yields new account's row id
            long rowId = dbHelper.getWritableDatabase().insert(
               DatabaseDescription.AccountData.TABLE_NAME, null, values);

            // if the account was inserted, create an appropriate Uri;
            // otherwise, throw an exception
            if (rowId > 0) { // SQLite row IDs start at 1
               newAccountUri = DatabaseDescription.AccountData.buildAccountUri(rowId);

               // notify observers that the database changed
               getContext().getContentResolver().notifyChange(uri, null);
            }
            else
               throw new SQLException(
                  getContext().getString(R.string.insert_failed) + uri);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_insert_uri) + uri);
      }

      return newAccountUri;
   }

   // update an existing account in the database
   @Override
   public int update(Uri uri, ContentValues values,
      String selection, String[] selectionArgs) {
      int numberOfRowsUpdated; // 1 if update successful; 0 otherwise

      switch (uriMatcher.match(uri)) {
         case ONE_ACCOUNT:
            // get from the uri the id of account to update
            String id = uri.getLastPathSegment();

            // update the account
            numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
               DatabaseDescription.AccountData.TABLE_NAME, values, DatabaseDescription.AccountData.COLUMN_ACCOUNT_ID + "=" + id,
               selectionArgs);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_update_uri) + uri);
      }

      // if changes were made, notify observers that the database changed
      if (numberOfRowsUpdated != 0) {
         getContext().getContentResolver().notifyChange(uri, null);
      }

      return numberOfRowsUpdated;
   }

   // delete an existing account from the database
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      int numberOfRowsDeleted;

      switch (uriMatcher.match(uri)) {
         case ONE_ACCOUNT:
            // get from the uri the id of account to update
            String id = uri.getLastPathSegment();

            // delete the account
            numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
               DatabaseDescription.AccountData.TABLE_NAME, DatabaseDescription.AccountData.COLUMN_ACCOUNT_ID + "=" + id, selectionArgs);
            break;
         default:
            throw new UnsupportedOperationException(
               getContext().getString(R.string.invalid_delete_uri) + uri);
      }

      // notify observers that the database changed
      if (numberOfRowsDeleted != 0) {
         getContext().getContentResolver().notifyChange(uri, null);
      }

      return numberOfRowsDeleted;
   }
}


/**************************************************************************
 * (C) Copyright 1992-2016 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/
