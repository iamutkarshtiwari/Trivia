package io.github.iamutkarshtiwari.trivia.models;

/**
 * Created by iamutkarshtiwari on 07/09/17.
 */


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.iamutkarshtiwari.trivia.R;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] images;
    private final String[] names;
    public CustomList(Activity context,
                      String[] web, Integer[] imageId) {
        super(context, R.layout.category_row, names);
        this.context = context;
        this.web = web;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.category_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.category_img);
        txtTitle.setText(images[position]);

        imageView.setImageResource(names[position]);
        return rowView;
    }
}