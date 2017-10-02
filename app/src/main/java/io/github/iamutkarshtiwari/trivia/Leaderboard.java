package io.github.iamutkarshtiwari.trivia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.iamutkarshtiwari.trivia.models.LeaderboardRecyclerViewAdapter;
import io.github.iamutkarshtiwari.trivia.models.ProfileRecyclerViewAdapter;

public class Leaderboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<String> number;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private LeaderboardRecyclerViewAdapter recyclerViewVerticalAdapter;
    private LinearLayoutManager horizontalLayout ;
    private View ChildView ;
    private int recyclerViewItemPosition ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            Log.e("ERROR: ", "Could not set up back button");
        }

        recyclerView = (RecyclerView) findViewById(R.id.leaderboard_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);


        recyclerViewVerticalAdapter = new LeaderboardRecyclerViewAdapter();
        horizontalLayout = new LinearLayoutManager(Leaderboard.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(horizontalLayout);
        recyclerView.setAdapter(recyclerViewVerticalAdapter);

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int totalItemCount = horizontalLayout.getItemCount();
//                int lastVisibleItem = horizontalLayout.findLastVisibleItemPosition();
//                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
//                    if (mOnLoadMoreListener != null) {
//                        mOnLoadMoreListener.onLoadMore();
//                    }
//                    isLoading = true;
//                }
//            }
//        });

//        recyclerViewVerticalAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override public void onLoadMore() {
//                Log.e("haint", "Load More");
//                mUsers.add(null);
//                mUserAdapter.notifyItemInserted(mUsers.size() - 1);
//                //Load more data for reyclerview
//                new Handler().postDelayed(new Runnable() {
//                    @Override public void run() {
//                        Log.e("haint", "Load More 2");
//                        //Remove loading item
//                        mUsers.remove(mUsers.size() - 1);
//                        mUserAdapter.notifyItemRemoved(mUsers.size());
//                        //Load data
//                        int index = mUsers.size();
//                        int end = index + 20;
//                        for (int i = index; i < end; i++) {
//                            User user = new User();
//                            user.setName("Name " + i);
//                            user.setEmail("alibaba" + i + "@gmail.com");
//                            mUsers.add(user);
//                        }
//                        mUserAdapter.notifyDataSetChanged();
//                        mUserAdapter.setLoaded();
//                    }
//                }, 5000);
//            }
//        });


        // Adding on item click listener to RecyclerView.
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(Leaderboard.this, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }
            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {
                ChildView = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if(ChildView != null && gestureDetector.onTouchEvent(motionEvent)) {
                    //Getting clicked value.
                    recyclerViewItemPosition = Recyclerview.getChildAdapterPosition(ChildView);
                    // Showing clicked item value on screen using toast message.
                    Toast.makeText(Leaderboard.this, number.get(recyclerViewItemPosition), Toast.LENGTH_LONG).show();

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });



//        for (int i = 0; i < 30; i++) {
//            User user = new User();
//            user.setName("Name " + i);
//            user.setEmail("alibaba" + i + "@gmail.com");
//            mUsers.add(user);
//        }


    }







    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Add what you wanna add
        super.onBackPressed();
    }
}
