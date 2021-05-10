// DatabaseDescription.java
// Describes the table name and column names for this app's database,
// and other information required by the ContentProvider
package com.smd.passwordvault.sql;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseDescription {
   // ContentProvider's name: typically the package name
   public static final String AUTHORITY =
      "com.smd.passwordvault.sql";

   // base URI used to interact with the ContentProvider
   private static final Uri BASE_CONTENT_URI =
      Uri.parse("content://" + AUTHORITY);

   // nested class defines contents of the Account/SiteData table
   public static final class AccountData implements BaseColumns {
      public static final String TABLE_NAME = "PV_ACCOUNT"; // table's name

      // Uri for the accounts table
      public static final Uri CONTENT_URI =
         BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

      // column names for Account/SiteData table's columns
      public static final String COLUMN_ACCOUNT_ID = "account_id";
      public static final String COLUMN_USER_ID = "user_id";
      public static final String COLUMN_NAME = "account_name";
      public static final String COLUMN_PASSWORD= "account_password";

      public static final String CREATE_RECIPES_TABLE =
              "CREATE TABLE " + TABLE_NAME + "(" +
                      COLUMN_ACCOUNT_ID + " integer primary key AUTOINCREMENT, " +
                      COLUMN_USER_ID + " TEXT, " +
                      COLUMN_NAME + " TEXT, " +
                      COLUMN_PASSWORD + " TEXT);";

      // creates a Uri for a specific account
      public static Uri buildAccountUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }
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
