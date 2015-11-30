package com.android.viglia.pdfaudiorecorderapp.fragments;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.viglia.pdfaudiorecorderapp.activities.PresentationPlayerActivity;
import com.android.viglia.pdfaudiorecorderapp.activities.R;
import com.android.viglia.pdfaudiorecorderapp.activities.SavePresentationActivity;
import com.android.viglia.pdfaudiorecorderapp.utility.FileUtility;
import com.android.viglia.pdfaudiorecorderapp.utility.Presentation;
import com.android.viglia.pdfaudiorecorderapp.utility.PresentationAdapter;
import com.android.viglia.pdfaudiorecorderapp.utility.SlideRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PresentationListFragment.OnPresentationListFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PresentationListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentationListFragment extends Fragment {

    List<Presentation> presentations;

    private ListView presentationListView;
    private PresentationAdapter adapter;
    private ActionMode actionMode;

    private int counter;
    private int selectedIndex;
    private List<Integer> selectedIndexes;
    private static final int EDIT_CODE = 1;

    private OnPresentationListFragmentListener mListener;



    public static PresentationListFragment newInstance() {
        PresentationListFragment fragment = new PresentationListFragment();
        return fragment;
    }

    public PresentationListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        presentations = getPresentations();
        adapter = new PresentationAdapter(getContext(),R.layout.presentation_item_layout,presentations);
        actionMode=null;
        counter = 0;
        selectedIndexes = new ArrayList<Integer>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_presentation_list, container, false);
        presentationListView = (ListView) view.findViewById(R.id.presentation_list);
        presentationListView.setAdapter(adapter);
        presentationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Presentation presentation = (Presentation) adapterView.getItemAtPosition(position);
                //when a presentation is selected --> start PresentationPlayerActivity to display it
                Intent playerIntent = new Intent(getActivity(), PresentationPlayerActivity.class);
                playerIntent.putExtra("presentation", presentation);
                startActivity(playerIntent);
            }
        });


        presentationListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        presentationListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode actionMode, int position, long id, boolean checked) {
                if (checked) {
                    counter++;
                    selectedIndexes.add(position);
                    if (counter > 1)
                        actionMode.invalidate();

                } else {
                    counter--;
                    selectedIndexes.remove(new Integer(position));
                    if (counter == 1)
                        actionMode.invalidate();
                }
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
                PresentationListFragment.this.actionMode = actionMode;
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.presentation_list_action_menu, menu);
                MenuItem editItem = menu.findItem(R.id.actionmenu_edit);
                MenuItem descriptionItem = menu.findItem(R.id.actionmenu_description);
                if (counter > 1) {
                    //if more than 1 item are selected then hide the edit and description options
                    editItem.setVisible(false);
                    descriptionItem.setVisible(false);
                }
                else {  //else show them
                    editItem.setVisible(true);
                    descriptionItem.setVisible(true);
                }
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
                MenuItem editItem = menu.findItem(R.id.actionmenu_edit);
                MenuItem descriptionItem = menu.findItem(R.id.actionmenu_description);
                if (counter > 1) {
                    editItem.setVisible(false);
                    descriptionItem.setVisible(false);
                }
                else {
                    editItem.setVisible(true);
                    descriptionItem.setVisible(true);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.actionmenu_delete:
                        //get the paths paths of the selected presentation
                        List<String> presentationsToDelete = new ArrayList<String>();
                        for (Integer item : selectedIndexes) {
                            presentationsToDelete.add(presentations.get(item).getDirPath());
                        }
                        showDelteDialog(presentationsToDelete);
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;

                    case R.id.actionmenu_edit:
                        editPresentation(presentations.get(selectedIndexes.get(0)));
                        return true;

                    case R.id.actionmenu_description:
                        showDescriptionDialog(presentations.get(selectedIndexes.get(0)));

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode actionMode) {

            }

        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            mListener = (OnPresentationListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPresentationListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnPresentationListFragmentListener {
        public void onPresentationSelected();
    }

    /**
     *This method return a list of presentations available
     *
     * @return A List of Presentation items
    */
    private List<Presentation> getPresentations(){
        List<Presentation> itemList = new ArrayList<Presentation>();
        File appDir = getActivity().getExternalFilesDir(null);
        for(File dir : appDir.listFiles()){
            File presentationJsonFile = new File(dir,"presentation.json");
            if(presentationJsonFile.exists()){
                try {
                    Presentation presentation = getPresentationFromJson(presentationJsonFile);
                    presentation.setDirPath(dir.getAbsolutePath());
                    itemList.add(presentation);
                }
                catch(FileNotFoundException e) {}
            }
        }
        return itemList;
    }

    /**
     *This method return a Presentation given a json file
     *
     * @param presentationJson a json file that defines the presentation
     * @return A Presentation item
     */
    private Presentation getPresentationFromJson(File presentationJson) throws FileNotFoundException {
        Presentation presentation = new Presentation();
        StringBuilder fileContents = new StringBuilder((int)presentationJson.length());
        Scanner scanner = new Scanner(presentationJson);
        while(scanner.hasNextLine())
            fileContents.append(scanner.nextLine());

        JSONObject jsonPresentation;
        try {
            jsonPresentation  = new JSONObject(fileContents.toString());
            presentation.setId(jsonPresentation.getString("id"));
            presentation.setTitle(jsonPresentation.getString("title"));
            presentation.setDescription(jsonPresentation.getString("description"));

            JSONArray slideArray = jsonPresentation.getJSONArray("slides");

            for(int i=0; i<slideArray.length(); i++){
                JSONObject slide = slideArray.getJSONObject(i);
                SlideRecord slideRecord = new SlideRecord(slide.getInt("slidePage"),slide.getLong("time"));
                presentation.addRecord(slideRecord);
            }

        } catch (JSONException e) {}

        return presentation;
    }

    private void editPresentation(Presentation presentation){

        Intent saveIntent = new Intent(getActivity(), SavePresentationActivity.class);
        saveIntent.putExtra("task","edit");
        saveIntent.putExtra("jsonFilePath", presentation.getDirPath() + "/presentation.json");
        //startActivityForResult(saveIntent, SAVE_PRESENTATION_CODE);
        startActivityForResult(saveIntent, EDIT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDIT_CODE && resultCode == Activity.RESULT_OK){
            actionMode.finish();
            List<Presentation> newPresentations = getPresentations();
            selectedIndexes.clear();
            counter = 0;
            adapter.clear();
            adapter.addAll(newPresentations);
            adapter.notifyDataSetChanged();
        }
        else{
            actionMode.finish();
            selectedIndexes.clear();
            counter = 0;
        }

    }

    //AsynkTask used to the delete a list of presentation
    private class DeleteDirsTask extends AsyncTask<List<String>, Void, Void> {
        List<Presentation> newPresentations;

        @Override
        protected Void doInBackground(List<String>... arg0) {
            List<String> dirs = arg0[0];
            for(String folder : dirs){
                FileUtility.deleteFolder(new File(folder));
            }

            newPresentations = getPresentations();
            PresentationListFragment.this.selectedIndexes.clear();
            return null;
        }

        protected void onPostExecute(Void result) {
            adapter.clear();
            adapter.addAll(newPresentations);
            adapter.notifyDataSetChanged();

        }

    }

    public void showDelteDialog(List<String> presentationsToDelete){
        final List<String> presentations = presentationsToDelete;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new DeleteDirsTask().execute(presentations);
            }
        });
        builder.setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                selectedIndexes.clear();
                counter = 0;
            }
        });
        builder.setTitle(R.string.cancel_presentations_title);
        builder.setMessage(R.string.cancel_presentations_message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void showDescriptionDialog(Presentation presentation){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.description_dialog, null);
        ((TextView)view.findViewById(R.id.descriptionContainer)).setText(presentation.getDescription());
        builder.setView(view);
        // Add the buttons
        builder.setPositiveButton(R.string.close_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.setTitle("Description");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
