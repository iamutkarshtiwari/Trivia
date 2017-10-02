package io.github.iamutkarshtiwari.trivia.models;

import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.iamutkarshtiwari.trivia.R;

/**
 * Created by iamutkarshtiwari on 17/09/17.
 */



public class LeaderboardRecyclerViewAdapter extends RecyclerView.Adapter<LeaderboardRecyclerViewAdapter.MyView> {

    private List<String> list;

    public class MyView extends RecyclerView.ViewHolder {

        public TextView playerName;
        public TextView playerRank;
        public TextView playerID;
        public TextView playerScore;
        public CircleImageView playerImageView;
        public ProgressBar progressBar;

        public MyView(View view) {
            super(view);
            playerImageView = (CircleImageView) view.findViewById(R.id.imageView);
            playerName = (TextView) view.findViewById(R.id.name);
            playerID = (TextView) view.findViewById(R.id.username);
            playerRank = (TextView) view.findViewById(R.id.rank);
            playerScore = (TextView) view.findViewById(R.id.score);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);


        }
    }


    public LeaderboardRecyclerViewAdapter() {
    }


    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_row, parent, false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(MyView holder, int position) {
        holder.playerRank.setText(Integer.toString(position));
    }


    @Override
    public int getItemCount() {
        return 10;
    }

//    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
//        this.mOnLoadMoreListener = mOnLoadMoreListener;
//    }
//
//    @Override public int getItemViewType(int position) {
//        return mUsers.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_ITEM) {
//            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_user_item, parent, false);
//            return new UserViewHolder(view);
//        } else if (viewType == VIEW_TYPE_LOADING) {
//            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_loading_item, parent, false);
//            return new LoadingViewHolder(view);
//        }
//        return null;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof UserViewHolder) {
//            User user = mUsers.get(position);
//            UserViewHolder userViewHolder = (UserViewHolder) holder;
//            userViewHolder.tvName.setText(user.getName());
//            userViewHolder.tvEmailId.setText(user.getEmail());
//        } else if (holder instanceof LoadingViewHolder) {
//            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
//            loadingViewHolder.progressBar.setIndeterminate(true);
//        }
//    }
//    @Override
//    public int getItemCount() {
//        return mUsers == null ? 0 : mUsers.size();
//    }
//
//    public void setLoaded() {
//        isLoading = false;
//    }

}

//
//class LeaderboardRecyclerViewAdapter extends RecyclerView.Adapter < LeaderboardRecyclerViewAdapter.ViewHolder > {
//    private final int VIEW_TYPE_ITEM = 0;
//    private final int VIEW_TYPE_LOADING = 1;
//    private OnLoadMoreListener mOnLoadMoreListener;
//    private boolean isLoading;
//    private int visibleThreshold = 5;
//    private int lastVisibleItem, totalItemCount;
//
//    public LeaderboardRecyclerViewAdapter() {
//
//    }
//
//    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
//        this.mOnLoadMoreListener = mOnLoadMoreListener;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return mUsers.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_ITEM) {
//            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_user_item, parent, false);
//            return new UserViewHolder(view);
//        } else if (viewType == VIEW_TYPE_LOADING) {
//            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_loading_item, parent, false);
//            return new LoadingViewHolder(view);
//        }
//        return null;
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof UserViewHolder) {
//            User user = mUsers.get(position);
//            UserViewHolder userViewHolder = (UserViewHolder) holder;
//            userViewHolder.tvName.setText(user.getName());
//            userViewHolder.tvEmailId.setText(user.getEmail());
//        } else if (holder instanceof LoadingViewHolder) {
//            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
//            loadingViewHolder.progressBar.setIndeterminate(true);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return mUsers == null ? 0 : mUsers.size();
//    }
//
//    public void setLoaded() {
//        isLoading = false;
//    }
//}