package com.android.viglia.pdfaudiorecorderapp.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.viglia.pdfaudiorecorderapp.fragments.PresentationListFragment;
import com.android.viglia.pdfaudiorecorderapp.fragments.PresentationListFragment.*;
import com.android.viglia.pdfaudiorecorderapp.fragments.RecorderFragment;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;


public class MainActivity extends AppCompatActivity implements OnPresentationListFragmentListener {

    //First We Declare Titles And Icons For Our Navigation Drawer List View
    //This Icons And Titles Are holded in an Array as you can see

    private String TITLES[] = {"Record","Presentations"};
    private int ICONS[] = {R.drawable.ic_mic_none_black_18dp,R.drawable.ic_event_note_black_18dp};


    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout drawer;

    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new MyAdapter(TITLES,ICONS);

        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);        // drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,drawer,R.string.app_name,R.string.app_name){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Add the RecorderFragment fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment contentFragment = (Fragment) fragmentManager.findFragmentById(R.id.content_frame);
        if(contentFragment==null) {
            contentFragment = RecorderFragment.newInstance();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.content_frame, contentFragment);
            //fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }



    } //end onCreate


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPresentationSelected() {

    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
        private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java


        // Creating a ViewHolder which extends the RecyclerView View Holder

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            int Holderid;

            TextView textView;
            ImageView imageView;



            public ViewHolder(View itemView,int ViewType) {
                super(itemView);
                itemView.setOnClickListener(this);
                textView = (TextView) itemView.findViewById(R.id.drawer_item_textView);
                imageView = (ImageView) itemView.findViewById(R.id.drawer_item_imageView);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment contentFragment = (Fragment) fragmentManager.findFragmentById(R.id.content_frame);

                switch (position){
                    case 0:
                        //we show the fragment that allow us to record a presentation
                        contentFragment = RecorderFragment.newInstance();
                        break;
                    case 1:
                        //we show the fragment that displays a list of recorded presentations
                        contentFragment = PresentationListFragment.newInstance();
                        break;
                }//end switch
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, contentFragment);
                fragmentTransaction.commit();
                drawer.closeDrawers();

            }


        }



        MyAdapter(String Titles[],int Icons[]){ // MyAdapter Constructor with titles and icons parameter
            mNavTitles = Titles;
            mIcons = Icons;
        }



        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item,parent,false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object
        }

        //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
        // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
        // which view type is being created 1 for item row
        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            holder.textView.setText(mNavTitles[position]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position]);// Settimg the image with array of our icons
        }

        // This method returns the number of items present in the list
        @Override
        public int getItemCount() {
            return mNavTitles.length;
        }
    }//end myAdapter

}
