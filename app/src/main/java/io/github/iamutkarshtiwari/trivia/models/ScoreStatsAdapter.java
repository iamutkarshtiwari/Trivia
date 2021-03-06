package io.github.iamutkarshtiwari.trivia.models;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.iamutkarshtiwari.trivia.R;

/**
 * Created by iamutkarshtiwari on 17/09/17.
 */



public class ScoreStatsAdapter extends RecyclerView.Adapter<ScoreStatsAdapter.MyView> {

    private List<String> list;
    private static final String TAG = "TRIVIA";

    public class MyView extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView categoryName;
        public TextView answeredQuestion;
        public TextView totalQuestion;

        public MyView(View view) {
            super(view);

            categoryName = (TextView) view.findViewById(R.id.category_name);
            answeredQuestion = (TextView) view.findViewById(R.id.category_answered);
            totalQuestion = (TextView) view.findViewById(R.id.category_total);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick " + getAdapterPosition());
        }
    }


    public ScoreStatsAdapter(List<String> horizontalList) {
        this.list = horizontalList;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_column, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(MyView holder, int position) {
        holder.answeredQuestion.setText(list.get(position));
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

}