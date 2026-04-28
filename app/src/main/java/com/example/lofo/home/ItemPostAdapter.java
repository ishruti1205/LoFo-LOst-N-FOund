package com.example.lofo.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lofo.R;
import com.example.lofo.model.ItemPost;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemPostAdapter extends ListAdapter<ItemPost, ItemPostAdapter.PostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemPost post);
    }

    private final OnItemClickListener listener;

    public ItemPostAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ItemPost> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ItemPost>() {
                @Override
                public boolean areItemsTheSame(@NonNull ItemPost a, @NonNull ItemPost b) {
                    return a.getPostId().equals(b.getPostId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull ItemPost a, @NonNull ItemPost b) {
                    return a.getStatus().equals(b.getStatus()) && a.getTitle().equals(b.getTitle());
                }
            };

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_home, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemImage;
        TextView tvItemFlag, tvItemTitle, tvItemCategory, tvItemDescription,
                tvItemLocation, tvItemTime;

        PostViewHolder(@NonNull View v) {
            super(v);
            ivItemImage      = v.findViewById(R.id.ivItemImage);
            tvItemFlag       = v.findViewById(R.id.tvItemFlag);
            tvItemTitle      = v.findViewById(R.id.tvItemTitle);
            tvItemCategory   = v.findViewById(R.id.tvItemCategory);
            tvItemDescription = v.findViewById(R.id.tvItemDescription);
            tvItemLocation   = v.findViewById(R.id.tvItemLocation);
            tvItemTime       = v.findViewById(R.id.tvItemTime);
        }

        void bind(ItemPost post, OnItemClickListener listener) {
            tvItemTitle.setText(post.getTitle());
            tvItemCategory.setText(post.getCategory());

            // Description (optional)
            String desc = post.getDescription();
            if (desc != null && !desc.isEmpty()) {
                tvItemDescription.setText(desc);
                tvItemDescription.setVisibility(View.VISIBLE);
            } else {
                tvItemDescription.setVisibility(View.GONE);
            }

            tvItemLocation.setText(post.getLocationName());

            // Relative time
            if (post.getCreatedAt() != null) {
                tvItemTime.setText(getRelativeTime(post.getCreatedAt().toDate()));
            }

            // Status flag
            switch (post.getStatus()) {
                case "lost":
                    tvItemFlag.setText("LOST");
                    tvItemFlag.setBackgroundResource(R.drawable.bg_flag_lost);
                    tvItemFlag.setVisibility(View.VISIBLE);
                    break;
                case "found":
                    tvItemFlag.setText("FOUND");
                    tvItemFlag.setBackgroundResource(R.drawable.bg_flag_found);
                    tvItemFlag.setVisibility(View.VISIBLE);
                    break;
                case "resolved":
                    tvItemFlag.setText("RESOLVED");
                    tvItemFlag.setBackgroundResource(R.drawable.bg_flag_resolved);
                    tvItemFlag.setVisibility(View.VISIBLE);
                    break;
                default:
                    tvItemFlag.setVisibility(View.GONE);
            }

            // Image via Glide (shows placeholder if null)
            String url = post.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_placeholder_item)
                        .centerCrop()
                        .into(ivItemImage);
            } else {
                ivItemImage.setImageResource(R.drawable.ic_placeholder_item);
                ivItemImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(post));
        }

        private String getRelativeTime(Date date) {
            long diff    = System.currentTimeMillis() - date.getTime();
            long minutes = diff / 60000;
            long hours   = diff / 3600000;
            long days    = diff / 86400000;
            if (minutes < 60) return minutes + " min ago";
            if (hours   < 24) return hours + " hr" + (hours == 1 ? "" : "s") + " ago";
            if (days    == 1) return "Yesterday";
            if (days    <  7) return days + " days ago";
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        }
    }
}