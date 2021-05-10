// DetailFragment.java
// Fragment subclass that displays one account's details
package com.smd.passwordvault.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smd.passwordvault.R;
import com.smd.passwordvault.activities.LoginActivity;
import com.smd.passwordvault.activities.MainActivity;
import com.smd.passwordvault.helpers.Constants;
import com.smd.passwordvault.helpers.EncryptionUtil;
import com.smd.passwordvault.sql.DatabaseDescription;

public class DetailFragment extends Fragment
   implements LoaderManager.LoaderCallbacks<Cursor> {

   private static final String TAG = "DetailFragment";

   // callback methods implemented by MainActivity
   public interface DetailFragmentListener {
      void onAccountDeleted(); // called when a account is deleted

      // pass Uri of account to edit to the DetailFragmentListener
      void onEditAccount(Uri accountUri);
   }

   private SharedPreferences sharedpreferences;
   private static final int ACCOUNT_LOADER = 0; // identifies the Loader

   private DetailFragmentListener listener; // MainActivity
   private Uri accountUri; // Uri of selected account

   private TextView nameTextView; // displays Account's name
   private TextView passwordTextView; // displays Account's password

   // set DetailFragmentListener when fragment attached
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      listener = (DetailFragmentListener) context;
   }

   // remove DetailFragmentListener when fragment detached
   @Override
   public void onDetach() {
      super.onDetach();
      listener = null;
   }

   // called when DetailFragmentListener's view needs to be created
   @Override
   public View onCreateView(
      LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      setHasOptionsMenu(true); // this fragment has menu items to display

      // getting the data which is stored in shared preferences.
      sharedpreferences = getContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);

      // get Bundle of arguments then extract the account's Uri
      Bundle arguments = getArguments();

      if (arguments != null)
         accountUri = arguments.getParcelable(MainActivity.ACCOUNT_URI);

      // inflate DetailFragment's layout
      View view =
         inflater.inflate(R.layout.fragment_detail, container, false);

      // get the EditTexts
      nameTextView = (TextView) view.findViewById(R.id.nameTextView);
      passwordTextView = (TextView) view.findViewById(R.id.passwordTextView);

      // load the account
      getLoaderManager().initLoader(ACCOUNT_LOADER, null, this);
      return view;
   }

   // display this fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_details_menu, menu);
   }

   // handle menu item selections
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_edit:
            listener.onEditAccount(accountUri); // pass Uri to listener
            return true;
         case R.id.action_delete:
            deleteAccount();
            return true;
         case R.id.action_logout:
            // clear shared preference data
            SharedPreferences.Editor editor = sharedpreferences.edit();
            // clear the logged in user id in shared preferences.
            editor.putInt(Constants.USER_ID_KEY, 0);
            editor.clear();
            editor.apply();
            // Navigate to LoginActivity
            Intent intentRegister = new Intent(getContext(), LoginActivity.class);
            startActivity(intentRegister);
            return true;
      }

      return super.onOptionsItemSelected(item);
   }

   // delete a account
   private void deleteAccount() {
      // use FragmentManager to display the confirmDelete DialogFragment
      confirmDelete.show(getFragmentManager(), "confirm delete");
   }

   // DialogFragment to confirm deletion of account
   private final DialogFragment confirmDelete =
      new DialogFragment() {
         // create an AlertDialog and return it
         @Override
         public Dialog onCreateDialog(Bundle bundle) {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder =
               new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete,
               new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(
                     DialogInterface dialog, int button) {

                     // use Activity's ContentResolver to invoke
                     // delete on the AccountDataContentProvider
                     getActivity().getContentResolver().delete(
                        accountUri, null, null);
                     listener.onAccountDeleted(); // notify listener
                  }
               }
            );

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create(); // return the AlertDialog
         }
      };

   // called by LoaderManager to create a Loader
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      // create an appropriate CursorLoader based on the id argument;
      // only one Loader in this fragment, so the switch is unnecessary
      CursorLoader cursorLoader;

      switch (id) {
         case ACCOUNT_LOADER:
            cursorLoader = new CursorLoader(getActivity(),
               accountUri, // Uri of account to display
               null, // null projection returns all columns
               null, // null selection returns all rows
               null, // no selection arguments
               null); // sort order
            break;
         default:
            cursorLoader = null;
            break;
      }

      return cursorLoader;
   }

   // called by LoaderManager when loading completes
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      // if the account exists in the database, display its data
      if (data != null && data.moveToFirst()) {
         // get the column index for each data item
         int nameIndex = data.getColumnIndex(DatabaseDescription.AccountData.COLUMN_NAME);
         int passwordIndex = data.getColumnIndex(DatabaseDescription.AccountData.COLUMN_PASSWORD);

         // fill TextViews with the retrieved data
         nameTextView.setText(data.getString(nameIndex));

         // decrypt the password to show it in plain text to the user
         String encPwd = data.getString(passwordIndex);
         String plainPwd = encPwd;
         try{
            plainPwd = EncryptionUtil.decryptPassword(encPwd);
         }
         catch (Exception ex){
            Log.e(TAG,"Error while decrypting the password for:" + nameTextView.getText());
         }
         passwordTextView.setText(plainPwd);
      }
   }

   // called by LoaderManager when the Loader is being reset
   @Override
   public void onLoaderReset(Loader<Cursor> loader) { }
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
