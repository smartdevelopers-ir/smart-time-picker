package ir.smartdevelopers.smartwheeltimepicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AmPmAdapter extends RecyclerView.Adapter<AmPmAdapter.PagerViewHolder> {
    private final String[] mTexts={"","ق.ظ","ب.ظ",""};
    private OnItemClickListener mOnItemClickListener;
    public AmPmAdapter() {

    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.whv_item_am_pm,parent,false);
        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        holder.bindView(mTexts[position]);
    }

    @Override
    public int getItemCount() {
        return mTexts.length;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class PagerViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumber;
        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumber=itemView.findViewById(R.id.whv_txtWheelChild);
            itemView.setOnClickListener(v->{
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(itemView,getAdapterPosition(),mTexts[getAdapterPosition()]);
                }
            });
        }
        void bindView(String number){
            txtNumber.setText(number);
        }
    }
}
