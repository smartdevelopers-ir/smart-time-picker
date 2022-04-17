package ir.smartdevelopers.smartwheeltimepicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class WheelAdapter extends RecyclerView.Adapter<WheelAdapter.PagerViewHolder> {
    private List<String> numbers;
    private OnItemClickListener mOnItemClickListener;
    private int mShift=0;
    public WheelAdapter(List<String> numbers) {
        this.numbers = numbers;
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.whv_item_wheel_child,parent,false);
        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()){
            if (Objects.equals(payloads.get(0),"shifted")){
                holder.bindView(numbers.get((position+mShift) % numbers.size() ));
            }

        }else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        holder.bindView(numbers.get((position+mShift) % numbers.size() ));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
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
                    mOnItemClickListener.onItemClicked(itemView,getAdapterPosition(),numbers.get((getAdapterPosition()+mShift) % numbers.size()));
                }
            });
        }
        void bindView(String number){
            txtNumber.setText(number);
        }
    }

    public void updateList(int startPos,int shift){
        mShift=shift;
        notifyItemRangeChanged(startPos,16,"shifted");
    }
}
