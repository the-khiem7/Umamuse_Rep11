package com.example.umamuse.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umamuse.R;
import com.example.umamuse.models.HorseBet;

import java.util.List;

public class HorseBetAdapter extends RecyclerView.Adapter<HorseBetAdapter.HorseBetViewHolder> {

    private Context context;
    private List<HorseBet> horseBets;

    public HorseBetAdapter(Context context, List<HorseBet> horseBets) {
        this.context = context;
        this.horseBets = horseBets;
    }

    @NonNull
    @Override
    public HorseBetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_horse_bet, parent, false);
        return new HorseBetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorseBetViewHolder holder, int position) {
        HorseBet horseBet = horseBets.get(position);
        
        holder.ivHorse.setImageResource(horseBet.getHorse().getImageRes());
        holder.tvHorseName.setText(horseBet.getHorse().getName());
        holder.tvOdds.setText("Odds: " + horseBet.getOdds());
        
        // Remove any existing TextWatcher to prevent multiple listeners
        if (holder.textWatcher != null) {
            holder.etBetAmount.removeTextChangedListener(holder.textWatcher);
        }
        
        // Display the existing bet if any
        if (horseBet.getBetAmount() > 0) {
            holder.tvExistingBet.setVisibility(View.VISIBLE);
            holder.tvExistingBet.setText("Current bet: $" + horseBet.getBetAmount());
            // Clear the EditText as it now represents a new bet
            holder.etBetAmount.setText("");
            holder.etBetAmount.setHint("Add more");
        } else {
            holder.tvExistingBet.setVisibility(View.GONE);
            holder.etBetAmount.setText("");
            holder.etBetAmount.setHint("Enter bet amount");
        }
        
        // Create new TextWatcher for this position
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        int amount = Integer.parseInt(s.toString());
                        horseBet.setNewBetAmount(amount);
                        
                        // Update potential winnings display
                        if (amount > 0 || horseBet.getBetAmount() > 0) {
                            holder.tvPotentialWinnings.setVisibility(View.VISIBLE);
                            holder.tvPotentialWinnings.setText("Potential win: $" + 
                                Math.round(horseBet.calculatePotentialWinnings() * 100) / 100.0f);
                        } else {
                            holder.tvPotentialWinnings.setVisibility(View.GONE);
                        }
                    } else {
                        horseBet.setNewBetAmount(0);
                        
                        // Still show potential winnings if there's an existing bet
                        if (horseBet.getBetAmount() > 0) {
                            holder.tvPotentialWinnings.setVisibility(View.VISIBLE);
                            holder.tvPotentialWinnings.setText("Potential win: $" + 
                                Math.round(horseBet.calculatePotentialWinnings() * 100) / 100.0f);
                        } else {
                            holder.tvPotentialWinnings.setVisibility(View.GONE);
                        }
                    }
                } catch (NumberFormatException e) {
                    horseBet.setNewBetAmount(0);
                }
            }
        };
        
        // Add the new TextWatcher
        holder.etBetAmount.addTextChangedListener(holder.textWatcher);
        
        // Initialize potential winnings display
        if (horseBet.getBetAmount() > 0) {
            holder.tvPotentialWinnings.setVisibility(View.VISIBLE);
            holder.tvPotentialWinnings.setText("Potential win: $" + 
                Math.round(horseBet.calculatePotentialWinnings() * 100) / 100.0f);
        } else {
            holder.tvPotentialWinnings.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return horseBets.size();
    }

    static class HorseBetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHorse;
        TextView tvHorseName;
        TextView tvOdds;
        TextView tvExistingBet;
        TextView tvPotentialWinnings;
        EditText etBetAmount;
        TextWatcher textWatcher; // Store reference to remove it later

        public HorseBetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHorse = itemView.findViewById(R.id.ivHorse);
            tvHorseName = itemView.findViewById(R.id.tvHorseName);
            tvOdds = itemView.findViewById(R.id.tvOdds);
            tvExistingBet = itemView.findViewById(R.id.tvExistingBet);
            tvPotentialWinnings = itemView.findViewById(R.id.tvPotentialWinnings);
            etBetAmount = itemView.findViewById(R.id.etBetAmount);
        }
    }
}