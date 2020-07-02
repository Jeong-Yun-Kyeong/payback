package kr.babylab.receipt;

import android.app.Activity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ListContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener onLoadMoreListener;
    private boolean isLoading;
    private Activity activity;
    private List<ListContact> listContacts;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    private Context ctx;

    public ListContactAdapter(RecyclerView recyclerView, List<ListContact> listContacts, Activity activity) {

        this.activity = activity;
        this.listContacts = listContacts;


        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return listContacts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.list_layout, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof UserViewHolder) {
            ListContact listContact = listContacts.get(position);
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            userViewHolder.idx.setText(listContact.getIdx());
            userViewHolder.reg_date.setText(listContact.getDate());
            userViewHolder.client_name.setText(listContact.getName());
            userViewHolder.phone_number.setText(listContact.getPhone());
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListContact listContact = listContacts.get(position);
                Intent intent = new Intent (v.getContext(), ListDetailActivity.class);
                //가져온 데이터와 고객명, 전화번호 추가해서 보내기
                intent.putExtra("idx", (String)listContact.getIdx());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listContacts == null ? 0 : listContacts.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        }
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView client_name;
        public TextView phone_number;
        public TextView reg_date;
        public TextView idx;

        public UserViewHolder(View view) {
            super(view);
            idx = itemView.findViewById(R.id.idx_textView);
            phone_number = (TextView) view.findViewById(R.id.phnumber_textView);
            client_name = (TextView) view.findViewById(R.id.name_textView);
            reg_date = (TextView) view.findViewById(R.id.data_textView);
        }
    }
}