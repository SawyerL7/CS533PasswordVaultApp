package com.smd.passwordvault.activities;

import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.smd.passwordvault.R;
import com.smd.passwordvault.fragments.AccountsFragment;
import com.smd.passwordvault.fragments.AddEditFragment;
import com.smd.passwordvault.fragments.DetailFragment;

public class MainActivity extends AppCompatActivity
        implements AccountsFragment.AccountsFragmentListener,
        DetailFragment.DetailFragmentListener,
        AddEditFragment.AddEditFragmentListener {

    private static final String TAG = "MainActivity";

    // key for storing a account's Uri in a Bundle passed to a fragment
    public static final String ACCOUNT_URI = "account_uri";

    private AccountsFragment accountsFragment; // displays account list

    // display AccountsFragment when MainActivity first loads
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_lock_black_24dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Log.v(TAG, "^^^^^^^ 1");
        String emailFromIntent = getIntent().getStringExtra("EMAIL");
        Log.v(TAG, "^^^^^^^ emailFromIntent:" + emailFromIntent);

        // if layout contains fragmentContainer, the phone layout is in use;
        // create and display a AccountsFragment
        if (savedInstanceState == null &&
                findViewById(R.id.fragmentContainer) != null) {
            // create AccountsFragment
            accountsFragment = new AccountsFragment();

            // add the fragment to the FrameLayout
            FragmentTransaction transaction =
                    getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, accountsFragment);
            transaction.commit(); // display AccountsFragment
        }
        else {
            if(savedInstanceState != null){
                Log.v(TAG,"----------------------- 1 NOT NULL");
                String email = (String) savedInstanceState.get("EMAIL") ;

                Log.v(TAG,"----------------------- email:" + email);
            }
            else {
                Log.v(TAG,"----------------------- 1 NULL");
            }

            accountsFragment =
                    (AccountsFragment) getSupportFragmentManager().
                            findFragmentById(R.id.accountsFragment);
        }
    }

    // display DetailFragment for selected account
    @Override
    public void onAccountSelected(Uri accountUri) {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAccount(accountUri, R.id.fragmentContainer);
        else { // tablet
            // removes top of back stack
            getSupportFragmentManager().popBackStack();

            displayAccount(accountUri, R.id.rightPaneContainer);
        }
    }

    // display AddEditFragment to add a new account
    @Override
    public void onAddAccount() {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, null);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, null);
    }

    // display a account
    private void displayAccount(Uri accountUri, int viewID) {
        DetailFragment detailFragment = new DetailFragment();

        // specify account's Uri as an argument to the DetailFragment
        Bundle arguments = new Bundle();
        arguments.putParcelable(ACCOUNT_URI, accountUri);
        detailFragment.setArguments(arguments);

        // use a FragmentTransaction to display the DetailFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes DetailFragment to display
    }

    // display fragment for adding a new or editing an existing account
    private void displayAddEditFragment(int viewID, Uri accountUri) {
        AddEditFragment addEditFragment = new AddEditFragment();

        // if editing existing account, provide accountUri as an argument
        if (accountUri != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ACCOUNT_URI, accountUri);
            addEditFragment.setArguments(arguments);
        }

        // use a FragmentTransaction to display the AddEditFragment
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, addEditFragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes AddEditFragment to display
    }

    // return to account list when displayed account deleted
    @Override
    public void onAccountDeleted() {
        // removes top of back stack
        getSupportFragmentManager().popBackStack();
        accountsFragment.updateContactList(); // refresh accounts
    }

    // display the AddEditFragment to edit an existing account
    @Override
    public void onEditAccount(Uri accountUri) {
        if (findViewById(R.id.fragmentContainer) != null) // phone
            displayAddEditFragment(R.id.fragmentContainer, accountUri);
        else // tablet
            displayAddEditFragment(R.id.rightPaneContainer, accountUri);
    }

    // update GUI after new account or updated account saved
    @Override
    public void onAddEditCompleted(Uri accountUri) {
        // removes top of back stack
        getSupportFragmentManager().popBackStack();
        accountsFragment.updateContactList(); // refresh accounts

        if (findViewById(R.id.fragmentContainer) == null) { // tablet
            // removes top of back stack
            getSupportFragmentManager().popBackStack();

            // on tablet, display account that was just added or edited
            displayAccount(accountUri, R.id.rightPaneContainer);
        }
    }
}