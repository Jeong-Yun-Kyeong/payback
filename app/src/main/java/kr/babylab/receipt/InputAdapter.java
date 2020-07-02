package kr.babylab.receipt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.List;

import static kr.babylab.receipt.R.drawable.ic_x_12;

public class InputAdapter extends RecyclerView.Adapter<InputAdapter.ViewHolder>{
    List<String> business_nums;
    List<String> receipt_nums;
    List<Integer> delete_btns;
    LayoutInflater inflater;

    public InputAdapter(Context ctx, List<String> business_nums, List<String> receipt_nums, List<Integer> delete_btns){
        this.business_nums = business_nums;
        this.receipt_nums = receipt_nums;
        this.delete_btns = delete_btns;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public InputAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.direct_list_item,parent,false);
        return new InputAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InputAdapter.ViewHolder holder, int position) {
        holder.business_num.setText(business_nums.get(position));
        holder.receipt_num.setText(receipt_nums.get(position));
        holder.delete_btn.setImageResource(delete_btns.get(position));
    }

    @Override
    public int getItemCount() {
        return receipt_nums.size();
    }

    public Collection get(List<String> receipt_nums) {
        return receipt_nums;
    }

//    public void add(String r) {
//        dir_result.add(r);
//        notifyItemInserted(dir_result.size() - 1);
//    }
//
//    public void addAll(List<String> dir_result) {
//        for (String result : dir_result) {
//            add(result);
//        }
//    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView business_num;
        private TextView receipt_num;
        private ImageView delete_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            business_num = itemView.findViewById(R.id.business_num);
            receipt_num = itemView.findViewById(R.id.receipt_num);

            delete_btn = itemView.findViewById(R.id.delete_btn);
            delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    business_nums.remove(getAdapterPosition());
                    receipt_nums.remove(getAdapterPosition());
                    delete_btns.remove(getAdapterPosition());
                    notifyDataSetChanged();}

            });
        }
    }
}


