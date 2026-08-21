package com.styro.medigo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.styro.medigo.R;
import com.styro.medigo.model.Medicine;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final Context context;
    private final List<Medicine> medicineList;
    private final OnMedicineClickListener clickListener;

    public interface OnMedicineClickListener {
        void onMedicineClick(Medicine medicine);
    }

    public MedicineAdapter(Context context, List<Medicine> medicineList,
                           OnMedicineClickListener clickListener) {
        this.context = context;
        this.medicineList = medicineList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);


        holder.tvName.setText(medicine.getName());
        holder.tvPrice.setText(context.getString(R.string.price_format, medicine.getPrice()));
        holder.tvCategory.setText(medicine.getCategory());


        Glide.with(context)
                .load(medicine.getImageUrl())
                .placeholder(R.drawable.placeholder_medicine)
                .error(R.drawable.placeholder_medicine)
                .override(300)
                .into(holder.ivMedicineImage);


        holder.ivPrescriptionBadge.setVisibility(
                medicine.isRequiresPrescription() ? View.VISIBLE : View.GONE
        );


        boolean inStock = medicine.isAvailable() && medicine.getStockQuantity() > 0;
        holder.tvOutOfStock.setVisibility(inStock ? View.GONE : View.VISIBLE);
        holder.itemView.setAlpha(inStock ? 1.0f : 0.6f);


        holder.itemView.setOnClickListener(v -> {
            if (clickListener == null) return;

            Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_click);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override public void onAnimationEnd(Animation a) {
                    clickListener.onMedicineClick(medicine);
                }
            });
            v.startAnimation(anim);
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {

        ImageView ivMedicineImage;
        ImageView ivPrescriptionBadge;
        TextView tvName;
        TextView tvPrice;
        TextView tvCategory;
        TextView tvOutOfStock;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedicineImage     = itemView.findViewById(R.id.ivMedicineImage);
            ivPrescriptionBadge = itemView.findViewById(R.id.ivPrescriptionBadge);
            tvName              = itemView.findViewById(R.id.tvMedicineName);
            tvPrice             = itemView.findViewById(R.id.tvMedicinePrice);
            tvCategory          = itemView.findViewById(R.id.tvMedicineCategory);
            tvOutOfStock        = itemView.findViewById(R.id.tvOutOfStock);
        }
    }
}