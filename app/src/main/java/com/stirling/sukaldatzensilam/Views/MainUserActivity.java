package com.stirling.sukaldatzensilam.Views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.stirling.sukaldatzensilam.R;
import com.stirling.sukaldatzensilam.Utils.BluetoothLEHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainUserActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    private int NUM_OF_COUNT;
    private boolean timelapseRunning = false;
    private Menu menu;
    private SubMenu modulos;
    private String correoUsu = "Correo del usuario";
    private FirebaseAuth auth;
    private String macbt;
    private int tempObtBT;

    BluetoothLEHelper ble;

    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Bind UI elements
        ButterKnife.bind(this);

        //Initialize toolbar
//        setSupportActionBar(toolbar);
        toolbar.setTitle("SukaldatzenSILAM");
        //Initialize Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //obtenemos el correo del usuario para mostrarlo en el navigation drawer
        correoUsu = FirebaseAuth.getInstance().getCurrentUser().getEmail(); //Null Object reference, por Login Google?
        //Listener de las opciones del navigation drawer
        navigationView.setNavigationItemSelectedListener
                (new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        // Handle navigation view item clicks here.
                        int id = item.getItemId();

                        switch (id)
                        {
                            case R.id.nav_newPot:
                                //Para emparejar nueva olla por bluetooth
                                //Nos movemos a activity de sincr. bluetooth
                                Intent btIntent = new Intent (MainUserActivity.this,
                                        BluetoothActivity.class);
                                startActivity(btIntent);
                                break;

                            case R.id.nav_signOut:
                                Toast.makeText(MainUserActivity.this, "Sesión cerrada",
                                        Toast.LENGTH_SHORT).show();
                                //Obtenemos instancia de FirebaseAuth actual
                                auth = FirebaseAuth.getInstance();
                                //Cerramos sesión de firebase
                                auth.signOut();
                                //Cambiamos a la activity Login
                                Intent loginIntent = new Intent(MainUserActivity.this,
                                        LoginActivity.class);
                                startActivity(loginIntent);
                                finish();
                                break;
                        }


                        drawer.closeDrawer(GravityCompat.START);

                        return true;
                    }

                });
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.textView);
        navUsername.setText(correoUsu);

        //Listener movimiento y estado del drawer
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override public void onDrawerOpened(View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {


            }
        });

        openNewFragment(new VisualizationFragment(), "VisualizationFragment",
                null, false);
        menu = navigationView.getMenu();
//        modulos = menu.addSubMenu("Módulos");


    }

    /**************************************************************************
     *  /name: openNewFragment
     *  /brief: This method opens the specified fragment in parameters
     **************************************************************************/
    public void openNewFragment(Fragment fragment, String fragmentTitle, Bundle extras,
                                boolean addToBackStack)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(extras);
        transaction.replace(R.id.frame_container, fragment, fragmentTitle);
        if(addToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }
}
