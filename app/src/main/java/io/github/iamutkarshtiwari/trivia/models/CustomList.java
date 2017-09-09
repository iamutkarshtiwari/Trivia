package io.github.iamutkarshtiwari.trivia.models;

/**
 * Created by iamutkarshtiwari on 07/09/17.
 */


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.iamutkarshtiwari.trivia.R;

public class CustomList extends ArrayAdapter<String> {

    public boolean[] checkSelectionList;
    private final Activity context;
    private final String[] names;

    public CustomList(Activity context, String[] categoryNames, boolean[] savedSelections) {
        super(context, android.R.layout.simple_list_item_multiple_choice, categoryNames);
        this.context = context;
        this.names = categoryNames;
        this.checkSelectionList = savedSelections;

    }

    /**
     * Updates the selection boolean array
     * @param array new boolean array
     */
    public void setCheckedSelections(boolean[] array) {
        this.checkSelectionList = array;
    }

    /**/
    @Override
    public int getCount() {
        return this.names.length;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.category_row, null, true);

            holder.checkBox = (CheckBox) convertView.findViewById(R.id.category_checkbox);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.category_name);
            holder.imgView = (ImageView) convertView.findViewById(R.id.category_img);

            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder) convertView.getTag();
        }

        int imgID = context.getResources().getIdentifier("category_" + (position + 1), "drawable", context.getPackageName());
        holder.txtTitle.setText(names[position]);
        holder.imgView.setImageResource(imgID);
        holder.checkBox.setChecked(checkSelectionList[position]);

        // Row click listener
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                holder.checkBox.setChecked(!checkSelectionList[position]);
                checkSelectionList[position] = !checkSelectionList[position];
            }
        });

        return convertView;
    }

    public class ViewHolder {
        private TextView txtTitle;
        private ImageView imgView;
        public CheckBox checkBox;
    }


}