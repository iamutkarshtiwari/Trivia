package io.github.iamutkarshtiwari.trivia.models;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.github.iamutkarshtiwari.trivia.R;

/**
 * Created by utkarshtiwari on 04/11/17.
 */

public class PrevQuestionsAdapter extends RecyclerView.Adapter<PrevQuestionsAdapter.MyView> {

    private List<Question> list;

    public class MyView extends RecyclerView.ViewHolder {

        public TextView question;
        public TextView answer;

        public MyView(View view) {
            super(view);

            question = (TextView) view.findViewById(R.id.question);
            answer = (TextView) view.findViewById(R.id.answer);

        }
    }


    public PrevQuestionsAdapter(List<Question> horizontalList) {
        this.list = horizontalList;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prev_question, parent, false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(MyView holder, int position) {
        holder.question.setText(list.get(position).getQuestion());
        holder.answer.setText(list.get(position).getAnswer());
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

}