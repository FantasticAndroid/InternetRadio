package com.android.internetradio.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internetradio.R;
import com.android.internetradio.activities.InternetRadioActivity;
import com.android.internetradio.models.FmStation;
import com.android.internetradio.utils.CommonUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class CategoryFmsRvAdapter extends RecyclerView.Adapter<CategoryFmsRvAdapter.CategoryFMHolder> {

    private InternetRadioActivity playerActivity;
    private List<FmStation> fmStationList;
    private LayoutInflater inflater;
    private RequestManager glideRequestManager;
    private RequestOptions glideOptions;

    /**
     * @param playerActivity
     */
    public CategoryFmsRvAdapter(InternetRadioActivity playerActivity, List<FmStation> fmStationList) {
        this.playerActivity = playerActivity;
        this.fmStationList = fmStationList;
        inflater = LayoutInflater.from(playerActivity);
        glideRequestManager = Glide.with(playerActivity);
        glideOptions = RequestOptions.fitCenterTransform().placeholder(R.drawable.ic_radio_small)
                .error(R.drawable.ic_radio_small);
    }

    @NonNull
    @Override
    public CategoryFMHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.rv_list_category_fm, parent, false);
        CategoryFMHolder holder = new CategoryFMHolder(view);
        return holder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryFMHolder holder, int position) {

        FmStation station = fmStationList.get(position);
        glideRequestManager.load(station.getFmIconUrl()).apply(glideOptions).into(holder.fmIconIv);
        holder.fmNameTv.setText(station.getFmName());
        holder.itemView.setTag(station);
        holder.itemView.setOnClickListener(clickListener);
        holder.horizontal_line.setBackgroundColor(CommonUtils.generateRandomColor());

        if (station.isPlaying) {
            holder.playIconIv.setVisibility(View.GONE);
        } else {
            holder.playIconIv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return fmStationList.size();
    }

    class CategoryFMHolder extends RecyclerView.ViewHolder {

        private TextView fmNameTv;
        private ImageView fmIconIv, playIconIv;
        private View horizontal_line;


        public CategoryFMHolder(View itemView) {
            super(itemView);
            fmNameTv = itemView.findViewById(R.id.tv_fm_name);
            fmIconIv = itemView.findViewById(R.id.siv_fm_image);
            horizontal_line = itemView.findViewById(R.id.v_line);
            playIconIv = itemView.findViewById(R.id.ic_play);
        }
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FmStation station = (FmStation) v.getTag();
            //////station.setPlaying(true);
            playerActivity.playFmStationWithFmCategoryAdaptor(station);
        }
    };
}
