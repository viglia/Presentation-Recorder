package com.viglia.android.pdfaudiorecorderapp.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.viglia.android.pdfaudiorecorderapp.activities.R;
import com.viglia.android.pdfaudiorecorderapp.utility.FileUtility;
import com.viglia.android.pdfaudiorecorderapp.utility.SlideRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavePresentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavePresentationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private ArrayList<SlideRecord> slideRecords;
    private String pdfPath;
    private JSONObject jsonPresentation;
    private String jsonFilePath;
    private String task;

    private Button saveButton;
    private Button cancelButton;
    private EditText editTitle;
    private EditText editDescription;

    private OnSaveFragmentListener mListener;


    public static SavePresentationFragment newInstance(Serializable presentation, Serializable pdfPath) {
        SavePresentationFragment fragment = new SavePresentationFragment();
        Bundle args = new Bundle();
        args.putSerializable("presentation", presentation);
        args.putString("pdfPath", (String) pdfPath);
        args.putString("task","save");
        fragment.setArguments(args);
        return fragment;
    }

    public static SavePresentationFragment newInstance(Serializable jsonFilePath) {
        SavePresentationFragment fragment = new SavePresentationFragment();
        Bundle args = new Bundle();
        args.putSerializable("jsonFilePath", jsonFilePath);
        args.putString("task", "edit");
        fragment.setArguments(args);
        return fragment;
    }

    public SavePresentationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            task = getArguments().getString("task");

            switch (task){
                case "save":
                    slideRecords = (ArrayList<SlideRecord>) getArguments().getSerializable("presentation");
                    pdfPath = getArguments().getString("pdfPath");
                    break;

                case "edit":
                    jsonFilePath = (String)getArguments().getSerializable("jsonFilePath");
                    jsonPresentation = getJsonFromFile(jsonFilePath);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_save_presentation, container, false);

        editTitle = (EditText)view.findViewById(R.id.editTitle);
        editDescription = (EditText)view.findViewById(R.id.editDescription);
        if(task.equals("edit")){
            //if it's an edit task, set the two editText with the title and description from the json
           try {
               editTitle.setText(jsonPresentation.getString("title"));
               editDescription.setText(jsonPresentation.getString("description"));
           }catch(JSONException exc){}
        }

        saveButton = (Button)view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isValidForm(editTitle.getText().toString(),editDescription.getText().toString())) {
                            if(task.equals("save"))
                                savePresentation();
                            else
                                editPresentation();
                        }
                        else
                        {
                            Toast.makeText(getContext(),R.string.form_validity_message,Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
        cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(task.equals("save"))
                    showDelteDialog();
                else
                    mListener.endCancel();
            }
        });

        return view;
    }


    private void savePresentation(){

        JSONArray slidesJsonArray = new JSONArray();
        for(SlideRecord slide : slideRecords){
            try {
                JSONObject slideJsonObject = new JSONObject();
                slideJsonObject.put("slidePage", slide.getSlide());
                slideJsonObject.put("time",slide.getSeconds());

                slidesJsonArray.put(slideJsonObject);
            }
            catch(JSONException exc){}
        }//end slide for
        JSONObject presentationJson = new JSONObject();
        PrintWriter printWriter = null;
        try {
            Calendar calendar = Calendar.getInstance();
            java.util.Date now = calendar.getTime();
            DateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
            String presentationId = date.format(now);

            presentationJson.put("id", presentationId);
            presentationJson.put("title", editTitle.getText());
            presentationJson.put("description", editDescription.getText());
            presentationJson.put("slides", slidesJsonArray);

            File presentationDir = new File(getActivity().getExternalFilesDir(null),presentationId);
            presentationDir.mkdir();
            File presentationFile = new File(presentationDir,"presentation.json");  //save the json we have just created

            printWriter = new PrintWriter(presentationFile);
            printWriter.write(presentationJson.toString());

            //copy the audio file into the presentation directory
            FileUtility.copy(new File(getActivity().getExternalFilesDir(null), "audio.mp4"), new File(presentationDir, "audio.mp4"));
            FileUtility.copy(new File(pdfPath),new File(presentationDir, "presentation.pdf"));  //do the same for the PDF file
            File temp = new File(getActivity().getExternalFilesDir(null), "audio.mp4");
            temp.delete();   //finally we delete the old audio file
        }catch(JSONException exc){}
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        finally {
            if(printWriter != null)
                printWriter.close();
            mListener.endSave();
        }
    }//end save presentation

    private void editPresentation(){
        PrintWriter printWriter = null;
        try {
            jsonPresentation.put("title", editTitle.getText().toString());
            jsonPresentation.put("description", editDescription.getText().toString());

            //overwrite the old file with the new data
            printWriter = new PrintWriter(new FileWriter(jsonFilePath));
            printWriter.write(jsonPresentation.toString());
        }catch(Exception exc){}
        finally {
            if(printWriter != null)
                printWriter.close();
            mListener.endSave();
        }

    }


    private JSONObject getJsonFromFile(String filePath){
        try {
            File jsonFile = new File(filePath);
            StringBuilder fileContents = new StringBuilder((int) jsonFile.length());
            Scanner scanner = new Scanner(jsonFile);
            while (scanner.hasNextLine())
                fileContents.append(scanner.nextLine());

            JSONObject jsonPresentation = new JSONObject(fileContents.toString());

            return jsonPresentation;
        }catch(FileNotFoundException | JSONException exc){return null;}
    }



    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            mListener = (OnSaveFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnRecorderFragmentListener");
        }
    }

    public interface OnSaveFragmentListener {
        public void endSave();
        public void endCancel();
    }

    public void showDelteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File temp = new File(getActivity().getExternalFilesDir(null), "audio.mp4");
                temp.delete();
                mListener.endSave();
            }
        });
        builder.setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.setTitle(R.string.cancel_save_title);
        builder.setMessage(R.string.cancel_save_message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean isValidForm(String title, String description){
        final String pattern = "(.|\\n)*[^\\s]+(.|\\n)*";
        return (title.matches(pattern) && description.matches(pattern));
    }
}
