package com.example.zeneblog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private List<BlogPost> blogList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BlogPost post);
    }

    public BlogAdapter(Context context, List<BlogPost> blogList, OnItemClickListener listener) {
        this.context = context;
        this.blogList = blogList;
        this.listener = listener;
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public BlogViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }

        public void bind(final BlogPost post, final OnItemClickListener listener) {
            titleTextView.setText(post.getTitle());
            descriptionTextView.setText(post.getDescription());
            itemView.setOnClickListener(v -> listener.onItemClick(post));
        }
    }

    @Override
    public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blog_post, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BlogViewHolder holder, int position) {
        holder.bind(blogList.get(position), listener);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fadein);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }
}
