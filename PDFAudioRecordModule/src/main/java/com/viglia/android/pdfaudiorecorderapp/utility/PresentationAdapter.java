package com.viglia.android.pdfaudiorecorderapp.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.viglia.android.pdfaudiorecorderapp.activities.R;

import java.util.List;

/**
 * Created by viglia on 11/13/15.
 */
public class PresentationAdapter extends ArrayAdapter<Presentation> {

    private List<Presentation> presentationList;

    public PresentationAdapter(Context context, int textViewResourceId,
                               List<Presentation> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.presentation_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)convertView.findViewById(R.id.presentation_item_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Presentation presentation = getItem(position);
        viewHolder.title.setText(presentation.getTitle());
        return convertView;
    }

    private class ViewHolder {
        public TextView title;
    }
}//end adapter